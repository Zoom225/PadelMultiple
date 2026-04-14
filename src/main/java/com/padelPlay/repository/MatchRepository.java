package com.padel.padel_backend.repository;


import com.padel.padel_backend.entity.Match;
import com.padel.padel_backend.entity.enums.StatutMatch;
import com.padel.padel_backend.entity.enums.TypeMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTypeMatchAndStatut(TypeMatch type, StatutMatch statut);
    List<Match> findByTerrainSiteId(Long siteId);
    List<Match> findByOrganisateurId(Long organisateurId);
    List<Match> findByDateAndStatut(LocalDate date, StatutMatch statut);
}
