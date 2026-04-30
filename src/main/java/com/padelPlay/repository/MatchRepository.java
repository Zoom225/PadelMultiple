package com.padelPlay.repository;


import com.padelPlay.entity.Match;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTypeMatchAndStatut(TypeMatch type, StatutMatch statut);
    List<Match> findByTerrainSiteId(Long siteId);
    List<Match> findByOrganisateurId(Long organisateurId);
    List<Match> findByDateDebutBetweenAndStatut(LocalDateTime start, LocalDateTime end, StatutMatch statut);
    List<Match> findByTerrainIdAndDateDebutBetweenAndStatutNot(Long terrainId, LocalDateTime start, LocalDateTime end, StatutMatch statut);
}
