package com.padelPlay.mapper;

import com.padelPlay.entity.Match;
import com.padelPlay.match.dto.MatchDto;
import org.springframework.stereotype.Service;

@Service
public class MatchMapper {

    public MatchDto toMatchDto(Match match) {
        if (match == null) {
            return null;
        }

        return new MatchDto(
                match.getId(),
                match.getTerrain().getId(),
                match.getTerrain().getNom(),
                match.getOrganisateur().getId(),
                match.getOrganisateur().getPrenom() + " " + match.getOrganisateur().getNom(),
                match.getDateDebut(),
                match.getDateFin(),
                match.getTypeMatch(),
                match.getStatut(),
                match.getNbJoueursActuels(),
                match.getPrixParJoueur()
        );
    }
}
