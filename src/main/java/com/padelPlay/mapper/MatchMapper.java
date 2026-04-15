package com.padelPlay.mapper;

import com.padelPlay.dto.request.MatchRequest;
import com.padelPlay.dto.response.MatchResponse;
import com.padelPlay.entity.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public Match toEntity(MatchRequest request) {
        return Match.builder()
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .typeMatch(request.getTypeMatch())
                .build();
        // terrain et organisateur résolus dans le service
    }

    public MatchResponse toResponse(Match match) {
        return MatchResponse.builder()
                .id(match.getId())
                .terrainId(match.getTerrain().getId())
                .terrainNom(match.getTerrain().getNom())
                .siteNom(match.getTerrain().getSite().getNom())
                .organisateurId(match.getOrganisateur().getId())
                .organisateurNom(match.getOrganisateur().getNom()
                        + " " + match.getOrganisateur().getPrenom())
                .date(match.getDate())
                .heureDebut(match.getHeureDebut())
                .heureFin(match.getHeureFin())
                .typeMatch(match.getTypeMatch())
                .statut(match.getStatut())
                .nbJoueursActuels(match.getNbJoueursActuels())
                .prixParJoueur(match.getPrixParJoueur())
                .dateConversionPublic(match.getDateConversionPublic())
                .build();
    }
}
