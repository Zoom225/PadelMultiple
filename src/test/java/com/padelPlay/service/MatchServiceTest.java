package com.padelPlay.service;

import com.padelPlay.entity.Match;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Site;
import com.padelPlay.entity.Terrain;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMembre;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.exception.BusinessException;
import com.padelPlay.exception.ResourceNotFoundException;
import com.padelPlay.mapper.MatchMapper;
import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto;
import com.padelPlay.repository.MatchRepository;
import com.padelPlay.repository.MembreRepository;
import com.padelPlay.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchServiceImpl Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MembreRepository membreRepository;
    @Mock
    private TerrainService terrainService;
    @Mock
    private MembreService membreService;
    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Membre organisateur;
    private Terrain terrain;
    private Site site;
    private CreateMatchRequest createMatchRequest;

    // Correction : Utiliser une date et une heure fixes pour des tests déterministes
    private final LocalDateTime VALID_MATCH_TIME = LocalDate.now().plusDays(10).atTime(15, 0);

    @BeforeEach
    void setUp() {
        site = new Site();
        site.setId(1L);
        site.setDureeMatchMinutes(90);
        site.setHeureOuverture(LocalTime.of(9, 0));
        site.setHeureFermeture(LocalTime.of(22, 0));

        organisateur = new Membre();
        organisateur.setId(1L);
        organisateur.setMatricule("user123");
        organisateur.setPrenom("John");
        organisateur.setNom("Doe");
        organisateur.setTypeMembre(TypeMembre.LIBRE);

        terrain = new Terrain();
        terrain.setId(1L);
        terrain.setNom("Court Central");
        terrain.setPrix(20.0);
        terrain.setSite(site);

        createMatchRequest = new CreateMatchRequest(
                terrain.getId(),
                VALID_MATCH_TIME,
                "PUBLIC"
        );
    }

    @Test
    @DisplayName("createMatch - Succès")
    void createMatch_ShouldSucceed_WhenAllRulesAreMet() {
        // Arrange
        when(membreRepository.findByMatricule("user123")).thenReturn(Optional.of(organisateur));
        when(terrainService.getById(terrain.getId())).thenReturn(terrain);
        when(membreService.hasOutstandingBalance(organisateur.getId())).thenReturn(false);
        when(membreService.hasActivePenalty(organisateur.getId())).thenReturn(false);
        when(matchRepository.findOverlappingMatches(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match matchToSave = invocation.getArgument(0);
            matchToSave.setId(1L);
            return matchToSave;
        });
        when(matchMapper.toMatchDto(any(Match.class))).thenReturn(new MatchDto(1L, 1L, "Court Central", 1L, "John Doe", VALID_MATCH_TIME, VALID_MATCH_TIME.plusMinutes(90), TypeMatch.PUBLIC, StatutMatch.PLANIFIE, 1, 5.0));

        // Act
        MatchDto result = matchService.createMatch(createMatchRequest, "user123");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("createMatch - Échoue si le créneau est déjà pris")
    void createMatch_ShouldFail_WhenSlotIsAlreadyBooked() {
        // Arrange
        // On mocke les validations qui doivent passer AVANT celle qu'on veut tester
        when(membreRepository.findByMatricule("user123")).thenReturn(Optional.of(organisateur));
        when(terrainService.getById(terrain.getId())).thenReturn(terrain);
        when(membreService.hasOutstandingBalance(anyLong())).thenReturn(false);
        when(membreService.hasActivePenalty(anyLong())).thenReturn(false);
        
        // C'est le mock qui déclenche le scénario de test
        when(matchRepository.findOverlappingMatches(any(), eq(VALID_MATCH_TIME), any(), any())).thenReturn(Collections.singletonList(new Match()));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            matchService.createMatch(createMatchRequest, "user123");
        });
        assertEquals("Ce créneau est déjà réservé sur le terrain : " + terrain.getId(), exception.getMessage());
    }
    
    @Test
    @DisplayName("createMatch - Échoue si le membre a un solde impayé")
    void createMatch_ShouldFail_WhenMemberHasOutstandingBalance() {
        when(membreRepository.findByMatricule("user123")).thenReturn(Optional.of(organisateur));
        when(terrainService.getById(terrain.getId())).thenReturn(terrain);
        when(membreService.hasOutstandingBalance(organisateur.getId())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> matchService.createMatch(createMatchRequest, "user123"));
        assertEquals("Le membre a un solde impayé et ne peut pas créer de match.", exception.getMessage());
    }

    @Test
    @DisplayName("createMatch - Échoue si le membre a une pénalité active")
    void createMatch_ShouldFail_WhenMemberHasActivePenalty() {
        when(membreRepository.findByMatricule("user123")).thenReturn(Optional.of(organisateur));
        when(terrainService.getById(terrain.getId())).thenReturn(terrain);
        when(membreService.hasActivePenalty(organisateur.getId())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> matchService.createMatch(createMatchRequest, "user123"));
        assertEquals("Le membre a une pénalité active et ne peut pas créer de match.", exception.getMessage());
    }

    @Test
    @DisplayName("createMatch - Échoue si le délai de réservation n'est pas respecté")
    void createMatch_ShouldFail_WhenBookingDelayIsNotMet() {
        // Arrange
        CreateMatchRequest shortNoticeRequest = new CreateMatchRequest(terrain.getId(), LocalDate.now().plusDays(2).atTime(15,0), "PUBLIC");
        when(membreRepository.findByMatricule("user123")).thenReturn(Optional.of(organisateur));
        when(terrainService.getById(terrain.getId())).thenReturn(terrain);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            matchService.createMatch(shortNoticeRequest, "user123");
        });
        assertTrue(exception.getMessage().contains("doit réserver au moins 5 jours à l'avance"));
    }
    
    @Test
    @DisplayName("createMatch - Échoue si le membre n'est pas trouvé")
    void createMatch_ShouldFail_WhenMemberNotFound() {
        // Arrange
        when(membreRepository.findByMatricule("unknownUser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            matchService.createMatch(createMatchRequest, "unknownUser");
        });
        assertEquals("Membre non trouvé pour l'utilisateur: unknownUser", exception.getMessage());
    }
}
