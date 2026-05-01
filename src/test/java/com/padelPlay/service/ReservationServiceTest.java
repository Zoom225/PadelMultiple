package com.padelPlay.service;

import com.padelPlay.entity.*;
import com.padelPlay.entity.enums.*;
import com.padelPlay.exception.BusinessException;
import com.padelPlay.exception.ResourceNotFoundException;
import com.padelPlay.repository.PaiementRepository;
import com.padelPlay.repository.ReservationRepository;
import com.padelPlay.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private MembreService membreService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Site site;
    private Site siteB;
    private Terrain terrain;
    private Membre organisateur;
    private Membre joueur;
    private Membre joueurSiteB;
    private Match matchPrive;
    private Match matchPublic;
    private Match matchComplet;

    @BeforeEach
    void setUp() {
        siteB = Site.builder().nom("Padel Club Paris").build();
        siteB.setId(2L);

        site = Site.builder().nom("Padel Club Lyon").build();
        site.setId(1L);

        terrain = Terrain.builder().nom("Court A").site(site).build();
        terrain.setId(1L);

        organisateur = Membre.builder()
                .matricule("G1001").nom("Martin").prenom("Lucas")
                .typeMembre(TypeMembre.GLOBAL).solde(0.0).site(null).build();
        organisateur.setId(1L);

        joueur = Membre.builder()
                .matricule("G1002").nom("Dupont").prenom("Julie")
                .typeMembre(TypeMembre.GLOBAL).solde(0.0).site(null).build();
        joueur.setId(2L);

        joueurSiteB = Membre.builder()
                .matricule("S20001").nom("Leclerc").prenom("Paul")
                .typeMembre(TypeMembre.SITE).solde(0.0).site(siteB).build();
        joueurSiteB.setId(3L);

        // Correction : Utiliser la structure correcte de l'entité Match
        LocalDateTime privateMatchTime = LocalDateTime.of(LocalDate.now().plusDays(25), LocalTime.of(15, 0));
        matchPrive = Match.builder()
                .terrain(terrain)
                .organisateur(organisateur)
                .dateDebut(privateMatchTime)
                .dateFin(privateMatchTime.plusMinutes(90))
                .typeMatch(TypeMatch.PRIVE)
                .statut(StatutMatch.PLANIFIE)
                .nbJoueursActuels(1)
                .prixTotal(60.0)
                .prixParJoueur(15.0)
                .build();
        matchPrive.setId(10L);

        LocalDateTime publicMatchTime = LocalDateTime.of(LocalDate.now().plusDays(25), LocalTime.of(17, 0));
        matchPublic = Match.builder()
                .terrain(terrain)
                .organisateur(organisateur)
                .dateDebut(publicMatchTime)
                .dateFin(publicMatchTime.plusMinutes(90))
                .typeMatch(TypeMatch.PUBLIC)
                .statut(StatutMatch.PLANIFIE)
                .nbJoueursActuels(1)
                .prixTotal(60.0)
                .prixParJoueur(15.0)
                .build();
        matchPublic.setId(11L);

        LocalDateTime fullMatchTime = LocalDateTime.of(LocalDate.now().plusDays(25), LocalTime.of(19, 0));
        matchComplet = Match.builder()
                .terrain(terrain)
                .organisateur(organisateur)
                .dateDebut(fullMatchTime)
                .dateFin(fullMatchTime.plusMinutes(90))
                .typeMatch(TypeMatch.PUBLIC)
                .statut(StatutMatch.COMPLET)
                .nbJoueursActuels(4)
                .prixTotal(60.0)
                .prixParJoueur(15.0)
                .build();
        matchComplet.setId(12L);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("✅ should create reservation for PUBLIC match with valid member")
        void shouldCreateReservationForPublicMatch() {
            when(matchService.getById(11L)).thenReturn(matchPublic);
            when(membreService.getById(2L)).thenReturn(joueur);
            when(membreService.hasActivePenalty(2L)).thenReturn(false);
            when(membreService.hasOutstandingBalance(2L)).thenReturn(false);
            when(reservationRepository.existsByMatchIdAndMembreId(11L, 2L)).thenReturn(false);
            when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Reservation result = reservationService.create(11L, 2L, 1L);

            assertThat(result).isNotNull();
            verify(paiementRepository, times(1)).save(any(Paiement.class));
        }

        @Test
        @DisplayName("❌ should throw BusinessException when match is COMPLET")
        void shouldThrowWhenMatchIsFull() {
            when(matchService.getById(12L)).thenReturn(matchComplet);
            when(membreService.getById(2L)).thenReturn(joueur);

            assertThatThrownBy(() -> reservationService.create(12L, 2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already full");
        }
    }

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("✅ should cancel reservation and decrement match players")
        void shouldCancelReservation() {
            Reservation reservation = Reservation.builder().match(matchPublic).membre(joueur).statut(StatutReservation.EN_ATTENTE).paiement(new Paiement()).build();
            reservation.setId(1L);

            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reservationService.cancel(1L);

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            verify(matchService, times(1)).decrementPlayers(matchPublic.getId());
        }
    }

    @Nested
    @DisplayName("confirm()")
    class ConfirmTests {

        @Test
        @DisplayName("✅ should confirm reservation and increment match players")
        void shouldConfirmReservation() {
            Reservation reservation = Reservation.builder().match(matchPublic).membre(joueur).statut(StatutReservation.EN_ATTENTE).build();
            reservation.setId(1L);

            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reservationService.confirm(1L);

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
            verify(matchService, times(1)).incrementPlayers(matchPublic.getId());
        }
    }
}
