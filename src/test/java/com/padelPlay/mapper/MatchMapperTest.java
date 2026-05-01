package com.padelPlay.mapper;

import com.padelPlay.entity.Match;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Site;
import com.padelPlay.entity.Terrain;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.match.dto.MatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MatchMapper Tests")
class MatchMapperTest {

    // Correction : Instancier directement le mapper, car ce n'est pas une interface MapStruct.
    private MatchMapper matchMapper;

    private Match match;
    private Membre organisateur;
    private Terrain terrain;

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2024, 5, 20, 14, 0);

    @BeforeEach
    void setUp() {
        // L'instanciation se fait ici pour garder les tests propres.
        matchMapper = new MatchMapper();

        Site site = new Site();
        site.setId(1L);
        site.setDureeMatchMinutes(90);
        site.setHeureOuverture(LocalTime.of(9, 0));
        site.setHeureFermeture(LocalTime.of(22, 0));

        organisateur = new Membre();
        organisateur.setId(1L);
        organisateur.setPrenom("John");
        organisateur.setNom("Doe");

        terrain = new Terrain();
        terrain.setId(1L);
        terrain.setNom("Court Central");
        terrain.setPrix(20.0);
        terrain.setSite(site);

        match = new Match();
        match.setId(1L);
        match.setTerrain(terrain);
        match.setOrganisateur(organisateur);
        // Correction : Utiliser 'dateDebut' au lieu de 'dateMatch'
        match.setDateDebut(FIXED_DATE);
        match.setDateFin(FIXED_DATE.plusMinutes(site.getDureeMatchMinutes()));
        match.setTypeMatch(TypeMatch.PUBLIC);
        match.setStatut(StatutMatch.PLANIFIE);
        // Correction : Utiliser 'nbJoueursActuels' au lieu de 'joueurs'
        match.setNbJoueursActuels(1);
        match.setPrixParJoueur(5.0);
    }

    @Test
    @DisplayName("toMatchDto - Doit mapper correctement Match vers MatchDto")
    void toMatchDto_shouldMapMatchToMatchDto() {
        // Act
        MatchDto resultDto = matchMapper.toMatchDto(match);

        // Assert
        assertNotNull(resultDto);
        assertAll("Vérification des propriétés de MatchDto",
            () -> assertEquals(match.getId(), resultDto.id()),
            () -> assertEquals(match.getTerrain().getId(), resultDto.terrainId()),
            // Correction : Le champ dans le DTO est 'nomTerrain'
            () -> assertEquals(match.getTerrain().getNom(), resultDto.nomTerrain()),
            () -> assertEquals(match.getOrganisateur().getId(), resultDto.organisateurId()),
            // Correction : Le champ dans le DTO est 'nomOrganisateur'
            () -> assertEquals("John Doe", resultDto.nomOrganisateur(), "Le nom de l'organisateur doit être concaténé"),
            // Correction : Le champ dans le DTO est 'dateDebut'
            () -> assertEquals(match.getDateDebut(), resultDto.dateDebut()),
            // Correction : Le champ dans le DTO est 'dateFin'
            () -> assertEquals(match.getDateFin(), resultDto.dateFin()),
            () -> assertEquals(match.getTypeMatch(), resultDto.typeMatch()),
            // Correction : Le champ dans le DTO est 'statut'
            () -> assertEquals(match.getStatut(), resultDto.statut()),
            () -> assertEquals(1, resultDto.nbJoueursActuels(), "Le nombre de joueurs doit être 1"),
            () -> assertEquals(match.getPrixParJoueur(), resultDto.prixParJoueur())
        );
    }

    @Test
    @DisplayName("toMatchDto - Doit retourner null si le Match est null")
    void toMatchDto_shouldReturnNull_whenMatchIsNull() {
        // Act
        MatchDto resultDto = matchMapper.toMatchDto(null);

        // Assert
        assertNull(resultDto, "Le DTO résultant doit être null quand l'entité source est null");
    }

    @Test
    @DisplayName("toMatchDto - Doit gérer les propriétés nulles sans erreur")
    void toMatchDto_shouldHandleNullProperties() {
        // Arrange
        Match matchWithNulls = new Match();
        matchWithNulls.setId(2L);
        matchWithNulls.setTerrain(null);
        matchWithNulls.setOrganisateur(null);
        matchWithNulls.setDateDebut(FIXED_DATE);
        matchWithNulls.setNbJoueursActuels(0);

        // Act
        MatchDto resultDto = matchMapper.toMatchDto(matchWithNulls);

        // Assert
        assertNotNull(resultDto);
        assertAll("Vérification du mapping avec des propriétés nulles",
            () -> assertEquals(2L, resultDto.id()),
            () -> assertNull(resultDto.terrainId(), "L'ID du terrain doit être null"),
            () -> assertNull(resultDto.nomTerrain(), "Le nom du terrain doit être null"),
            () -> assertNull(resultDto.organisateurId(), "L'ID de l'organisateur doit être null"),
            () -> assertNull(resultDto.nomOrganisateur(), "Le nom de l'organisateur doit être null"),
            () -> assertEquals(0, resultDto.nbJoueursActuels(), "Le nombre de joueurs doit être 0"),
            () -> assertEquals(FIXED_DATE, resultDto.dateDebut())
        );
    }
}
