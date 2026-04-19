package com.padelPlay.service;

import com.padelPlay.entity.*;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.entity.enums.TypeMembre;
import com.padelPlay.exception.BusinessException;
import com.padelPlay.exception.ResourceNotFoundException;
import com.padelPlay.repository.MatchRepository;
import com.padelPlay.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MembreService membreService;

    @Mock
    private TerrainService terrainService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Site site;
    private Terrain terrain;
    private Membre organisateurGlobal;
    private Membre organisateurSite;
    private Membre organisateurLibre;
    private Match matchPrive;
    private Match matchPublic;

    @BeforeEach
    void setUp() {
        site = Site.builder()
                .nom("Padel Club Lyon")
                .adresse("12 rue de la République")
                .heureOuverture(LocalTime.of(8, 0))
                .heureFermeture(LocalTime.of(22, 0))
                .dureeMatchMinutes(90)
                .dureeEntreMatchMinutes(15)
                .anneeCivile(2025)
                .build();

        // ← initialiser la liste vide pour éviter NullPointerException
        site.setJoursFermeture(new ArrayList<>());

        terrain = Terrain.builder()
                .nom("Court A")
                .site(site)
                .build();

        organisateurGlobal = Membre.builder()
                .matricule("G1001")
                .nom("Martin")
                .prenom("Lucas")
                .typeMembre(TypeMembre.GLOBAL)
                .solde(0.0)
                .build();

        organisateurSite = Membre.builder()
                .matricule("S10001")
                .nom("Bernard")
                .prenom("Tom")
                .typeMembre(TypeMembre.SITE)
                .solde(0.0)
                .site(site)
                .build();

        organisateurLibre = Membre.builder()
                .matricule("L10001")
                .nom("Petit")
                .prenom("Alex")
                .typeMembre(TypeMembre.LIBRE)
                .solde(0.0)
                .build();

        matchPrive = Match.builder()
                .terrain(terrain)
                .organisateur(organisateurGlobal)
                .date(LocalDate.now().plusDays(25))
                .heureDebut(LocalTime.of(15, 0))
                .heureFin(LocalTime.of(16, 30))
                .typeMatch(TypeMatch.PRIVE)
                .statut(StatutMatch.PLANIFIE)
                .nbJoueursActuels(1)
                .prixTotal(60.0)
                .prixParJoueur(15.0)
                .build();

        matchPublic = Match.builder()
                .terrain(terrain)
                .organisateur(organisateurGlobal)
                .date(LocalDate.now().plusDays(25))
                .heureDebut(LocalTime.of(17, 0))
                .heureFin(LocalTime.of(18, 30))
                .typeMatch(TypeMatch.PUBLIC)
                .statut(StatutMatch.PLANIFIE)
                .nbJoueursActuels(1)
                .prixTotal(60.0)
                .prixParJoueur(15.0)
                .build();
    }

    // ================================================================
    // CREATE
    // ================================================================
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("✅ should create a PRIVE match with valid organizer GLOBAL — 25 days in advance")
        void shouldCreatePriveMatchWithGlobalMember() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);
            when(matchRepository.findByTerrainSiteId(any())).thenReturn(List.of());
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(25))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            Match result = matchService.create(match, 1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getTypeMatch()).isEqualTo(TypeMatch.PRIVE);
            assertThat(result.getStatut()).isEqualTo(StatutMatch.PLANIFIE);
            assertThat(result.getNbJoueursActuels()).isEqualTo(1);
            assertThat(result.getPrixTotal()).isEqualTo(60.0);
            assertThat(result.getPrixParJoueur()).isEqualTo(15.0);
            assertThat(result.getHeureFin()).isEqualTo(LocalTime.of(16, 30));
        }

        @Test
        @DisplayName("✅ should create a PUBLIC match with valid organizer GLOBAL")
        void shouldCreatePublicMatch() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);
            when(matchRepository.findByTerrainSiteId(any())).thenReturn(List.of());
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(25))
                    .heureDebut(LocalTime.of(17, 0))
                    .typeMatch(TypeMatch.PUBLIC)
                    .build();

            Match result = matchService.create(match, 1L, 1L);

            assertThat(result.getTypeMatch()).isEqualTo(TypeMatch.PUBLIC);
            assertThat(result.getNbJoueursActuels()).isEqualTo(1);
        }

        @Test
        @DisplayName("✅ should auto calculate heureFin from site dureeMatchMinutes")
        void shouldAutoCalculateHeureFin() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);
            when(matchRepository.findByTerrainSiteId(any())).thenReturn(List.of());
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(25))
                    .heureDebut(LocalTime.of(10, 0)) // début 10h00
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            Match result = matchService.create(match, 1L, 1L);

            // site.dureeMatchMinutes = 90 → fin = 10h00 + 90min = 11h30
            assertThat(result.getHeureFin()).isEqualTo(LocalTime.of(11, 30));
        }

        @Test
        @DisplayName("❌ should throw BusinessException when organizer has outstanding balance")
        void shouldThrowWhenOrganizerHasBalance() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(true);

            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(25))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("outstanding balance");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when organizer has active penalty")
        void shouldThrowWhenOrganizerHasPenalty() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(true);

            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(25))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("active penalty");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when GLOBAL books less than 21 days in advance")
        void shouldThrowWhenGlobalBooksTooLate() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);

            // GLOBAL doit réserver 21 jours avant → 15 jours c'est trop tard
            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(15))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("21 days in advance");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when SITE books less than 14 days in advance")
        void shouldThrowWhenSiteBooksTooLate() {
            when(membreService.getById(1L)).thenReturn(organisateurSite);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);

            // SITE doit réserver 14 jours avant → 10 jours c'est trop tard
            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(10))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("14 days in advance");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when LIBRE books less than 5 days in advance")
        void shouldThrowWhenLibreBooksTooLate() {
            when(membreService.getById(1L)).thenReturn(organisateurLibre);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);

            // LIBRE doit réserver 5 jours avant → 3 jours c'est trop tard
            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(3))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("5 days in advance");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ should allow GLOBAL to book exactly 21 days in advance")
        void shouldAllowGlobalToBookExactly21Days() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);
            when(matchRepository.findByTerrainSiteId(any())).thenReturn(List.of());
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // exactement 21 jours → doit passer
            Match match = Match.builder()
                    .date(LocalDate.now().plusDays(21))
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatCode(() -> matchService.create(match, 1L, 1L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("❌ should throw BusinessException when slot already taken on same date")
        void shouldThrowWhenSlotAlreadyTaken() {
            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);

            LocalDate matchDate = LocalDate.now().plusDays(25);

            // un match existe déjà sur ce terrain à cette date
            Match existingMatch = Match.builder()
                    .terrain(terrain)
                    .date(matchDate)
                    .statut(StatutMatch.PLANIFIE)
                    .build();

            when(matchRepository.findByTerrainSiteId(any()))
                    .thenReturn(List.of(existingMatch));

            Match newMatch = Match.builder()
                    .date(matchDate)
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(newMatch, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already booked");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when site is closed on match date")
        void shouldThrowWhenSiteIsClosed() {
            LocalDate closedDate = LocalDate.now().plusDays(25);

            JourFermeture jourFermeture = JourFermeture.builder()
                    .date(closedDate)
                    .raison("Maintenance")
                    .global(false)
                    .site(site)
                    .build();

            site.setJoursFermeture(List.of(jourFermeture));

            when(membreService.getById(1L)).thenReturn(organisateurGlobal);
            when(terrainService.getById(1L)).thenReturn(terrain);
            when(membreService.hasOutstandingBalance(1L)).thenReturn(false);
            when(membreService.hasActivePenalty(1L)).thenReturn(false);
            when(matchRepository.findByTerrainSiteId(any())).thenReturn(List.of());

            Match match = Match.builder()
                    .date(closedDate)
                    .heureDebut(LocalTime.of(15, 0))
                    .typeMatch(TypeMatch.PRIVE)
                    .build();

            assertThatThrownBy(() -> matchService.create(match, 1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("closed");

            verify(matchRepository, never()).save(any());
        }
    }

    // ================================================================
    // GET
    // ================================================================
    @Nested
    @DisplayName("getById()")
    class GetTests {

        @Test
        @DisplayName("✅ should return match when id exists")
        void shouldReturnMatchById() {
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));

            Match result = matchService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getTypeMatch()).isEqualTo(TypeMatch.PRIVE);
        }

        @Test
        @DisplayName("❌ should throw ResourceNotFoundException when id not found")
        void shouldThrowWhenMatchNotFound() {
            when(matchRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Match not found with id : 99");
        }

        @Test
        @DisplayName("✅ should return only PUBLIC PLANIFIE matches")
        void shouldReturnOnlyPublicAvailableMatches() {
            when(matchRepository.findByTypeMatchAndStatut(TypeMatch.PUBLIC, StatutMatch.PLANIFIE))
                    .thenReturn(List.of(matchPublic));

            List<Match> result = matchService.getPublicAvailableMatches();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTypeMatch()).isEqualTo(TypeMatch.PUBLIC);
            assertThat(result.getFirst().getStatut()).isEqualTo(StatutMatch.PLANIFIE);
        }
    }

    // ================================================================
    // CONVERT TO PUBLIC
    // ================================================================
    @Nested
    @DisplayName("convertToPublic()")
    class ConvertToPublicTests {

        @Test
        @DisplayName("✅ should convert PRIVE match to PUBLIC and apply penalty to organizer")
        void shouldConvertPriveToPublic() {
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            matchService.convertToPublic(1L);

            assertThat(matchPrive.getTypeMatch()).isEqualTo(TypeMatch.PUBLIC);
            assertThat(matchPrive.getDateConversionPublic()).isNotNull();

            // pénalité appliquée à l'organisateur
            verify(membreService, times(1)).addPenalty(matchPrive.getOrganisateur().getId());
        }

        @Test
        @DisplayName("❌ should throw BusinessException when match is already PUBLIC")
        void shouldThrowWhenMatchAlreadyPublic() {
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPublic));

            assertThatThrownBy(() -> matchService.convertToPublic(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already public");

            verify(membreService, never()).addPenalty(any());
        }
    }

    // ================================================================
    // INCREMENT / DECREMENT PLAYERS
    // ================================================================
    @Nested
    @DisplayName("incrementPlayers() and decrementPlayers()")
    class PlayersTests {

        @Test
        @DisplayName("✅ should increment nbJoueursActuels from 1 to 2")
        void shouldIncrementPlayers() {
            matchPrive.setNbJoueursActuels(1);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            matchService.incrementPlayers(1L);

            assertThat(matchPrive.getNbJoueursActuels()).isEqualTo(2);
            assertThat(matchPrive.getStatut()).isEqualTo(StatutMatch.PLANIFIE);
        }

        @Test
        @DisplayName("✅ should set statut to COMPLET when nbJoueursActuels reaches 4")
        void shouldSetCompletWhenFull() {
            matchPrive.setNbJoueursActuels(3);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            matchService.incrementPlayers(1L);

            assertThat(matchPrive.getNbJoueursActuels()).isEqualTo(4);
            assertThat(matchPrive.getStatut()).isEqualTo(StatutMatch.COMPLET);
        }

        @Test
        @DisplayName("❌ should throw BusinessException when match is already full — cannot add 5th player")
        void shouldThrowWhenMatchAlreadyFull() {
            matchPrive.setNbJoueursActuels(4);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));

            assertThatThrownBy(() -> matchService.incrementPlayers(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already full");
        }

        @Test
        @DisplayName("✅ should decrement nbJoueursActuels and reset statut to PLANIFIE")
        void shouldDecrementPlayers() {
            matchPrive.setNbJoueursActuels(4);
            matchPrive.setStatut(StatutMatch.COMPLET);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            matchService.decrementPlayers(1L);

            assertThat(matchPrive.getNbJoueursActuels()).isEqualTo(3);
            assertThat(matchPrive.getStatut()).isEqualTo(StatutMatch.PLANIFIE);
        }

        @Test
        @DisplayName("❌ should throw BusinessException when decrementing below 0 players")
        void shouldThrowWhenDecrementingBelowZero() {
            matchPrive.setNbJoueursActuels(0);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));

            assertThatThrownBy(() -> matchService.decrementPlayers(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("0 players");
        }
    }

    // ================================================================
    // IS MATCH FULL
    // ================================================================
    @Nested
    @DisplayName("isMatchFull()")
    class IsMatchFullTests {

        @Test
        @DisplayName("✅ should return true when match has 4 players")
        void shouldReturnTrueWhenFull() {
            matchPrive.setNbJoueursActuels(4);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));

            assertThat(matchService.isMatchFull(1L)).isTrue();
        }

        @Test
        @DisplayName("✅ should return false when match has less than 4 players")
        void shouldReturnFalseWhenNotFull() {
            matchPrive.setNbJoueursActuels(2);
            when(matchRepository.findById(1L)).thenReturn(Optional.of(matchPrive));

            assertThat(matchService.isMatchFull(1L)).isFalse();
        }
    }

    // ================================================================
    // SCHEDULER
    // ================================================================
    @Nested
    @DisplayName("checkAndConvertExpiredPrivateMatches()")
    class SchedulerTests {

        @Test
        @DisplayName("✅ should convert all PRIVE PLANIFIE matches scheduled for tomorrow with less than 4 players")
        void shouldConvertExpiredPrivateMatches() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            Match expiredMatch = Match.builder()
                    .terrain(terrain)
                    .organisateur(organisateurGlobal)
                    .date(tomorrow)
                    .typeMatch(TypeMatch.PRIVE)
                    .statut(StatutMatch.PLANIFIE)
                    .nbJoueursActuels(2) // pas complet
                    .build();

            when(matchRepository.findByDateAndStatut(tomorrow, StatutMatch.PLANIFIE))
                    .thenReturn(List.of(expiredMatch));
            when(matchRepository.findById(any())).thenReturn(Optional.of(expiredMatch));
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            matchService.checkAndConvertExpiredPrivateMatches();

            assertThat(expiredMatch.getTypeMatch()).isEqualTo(TypeMatch.PUBLIC);
            verify(membreService, times(1)).addPenalty(any());
        }

        @Test
        @DisplayName("✅ should NOT convert PRIVE match that is already full — 4 players")
        void shouldNotConvertFullPrivateMatch() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            Match fullMatch = Match.builder()
                    .terrain(terrain)
                    .organisateur(organisateurGlobal)
                    .date(tomorrow)
                    .typeMatch(TypeMatch.PRIVE)
                    .statut(StatutMatch.PLANIFIE)
                    .nbJoueursActuels(4) // complet → ne doit pas être converti
                    .build();

            when(matchRepository.findByDateAndStatut(tomorrow, StatutMatch.PLANIFIE))
                    .thenReturn(List.of(fullMatch));

            matchService.checkAndConvertExpiredPrivateMatches();

            // le match reste PRIVE
            assertThat(fullMatch.getTypeMatch()).isEqualTo(TypeMatch.PRIVE);
            verify(membreService, never()).addPenalty(any());
        }

        @Test
        @DisplayName("✅ should NOT convert PUBLIC match during scheduler run")
        void shouldNotConvertPublicMatch() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            Match publicMatch = Match.builder()
                    .terrain(terrain)
                    .organisateur(organisateurGlobal)
                    .date(tomorrow)
                    .typeMatch(TypeMatch.PUBLIC)
                    .statut(StatutMatch.PLANIFIE)
                    .nbJoueursActuels(1)
                    .build();

            when(matchRepository.findByDateAndStatut(tomorrow, StatutMatch.PLANIFIE))
                    .thenReturn(List.of(publicMatch));

            matchService.checkAndConvertExpiredPrivateMatches();

            assertThat(publicMatch.getTypeMatch()).isEqualTo(TypeMatch.PUBLIC);
            verify(membreService, never()).addPenalty(any());
        }
    }
}