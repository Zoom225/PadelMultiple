package com.padelPlay.match.dto;

import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import java.time.LocalDateTime;

public record MatchDto(
    Long id,
    Long terrainId,
    String nomTerrain,
    Long organisateurId,
    String nomOrganisateur,
    LocalDateTime dateDebut,
    LocalDateTime dateFin,
    TypeMatch typeMatch,
    StatutMatch statut,
    Integer nbJoueursActuels,
    Double prixParJoueur
) {}
