package com.padelPlay.service.impl;

import com.padelPlay.entity.Match;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Terrain;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.exception.BusinessException;
import com.padelPlay.exception.ResourceNotFoundException;
import com.padelPlay.repository.MatchRepository;
import com.padelPlay.service.MatchService;
import com.padelPlay.service.MembreService;
import com.padelPlay.service.TerrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private static final int MAX_PLAYERS     = 4;
    private static final double MATCH_PRICE  = 60.0;

    private final MatchRepository matchRepository;
    private final MembreService membreService;
    private final TerrainService terrainService;

    @Override
    @Transactional
    public Match create(Match match, Long organisateurId, Long terrainId) {
        Membre organisateur = membreService.getById(organisateurId);
        Terrain terrain     = terrainService.getById(terrainId);

        // règle : solde dû bloque la création
        if (membreService.hasOutstandingBalance(organisateurId)) {
            throw new BusinessException("Member has an outstanding balance and cannot create a match");
        }

        // règle : pénalité active bloque la création
        if (membreService.hasActivePenalty(organisateurId)) {
            throw new BusinessException("Member has an active penalty and cannot create a match");
        }

        // règle : vérifier le délai de réservation selon le type de membre
        validateBookingDelay(organisateur, match.getDate());

        // règle : vérifier que le créneau est disponible sur ce terrain
        if (!isSlotAvailable(terrainId, match.getDate())) {
            throw new BusinessException("This slot is already booked on terrain : " + terrainId);
        }

        // règle : vérifier que le site n'est pas fermé ce jour là
        validateSiteNotClosed(terrain, match.getDate());

        // calcul des heures de fin selon la config du site
        LocalTime heureFin = match.getHeureDebut()
                .plusMinutes(terrain.getSite().getDureeMatchMinutes());

        match.setOrganisateur(organisateur);
        match.setTerrain(terrain);
        match.setHeureFin(heureFin);
        match.setNbJoueursActuels(1);
        match.setPrixTotal(MATCH_PRICE);
        match.setPrixParJoueur(MATCH_PRICE / MAX_PLAYERS);
        match.setStatut(StatutMatch.PLANIFIE);

        log.info("Match created by member {} on terrain {} at {}",
                organisateurId, terrainId, match.getDate());

        return matchRepository.save(match);
    }

    @Override
    public Match getById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id : " + id));
    }

    @Override
    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    @Override
    public List<Match> getPublicAvailableMatches() {
        return matchRepository.findByTypeMatchAndStatut(
                TypeMatch.PUBLIC,
                StatutMatch.PLANIFIE
        );
    }

    @Override
    public List<Match> getBySiteId(Long siteId) {
        return matchRepository.findByTerrainSiteId(siteId);
    }

    @Override
    public List<Match> getByOrganisateurId(Long organisateurId) {
        return matchRepository.findByOrganisateurId(organisateurId);
    }

    @Override
    @Transactional
    public void convertToPublic(Long matchId) {
        Match match = getById(matchId);

        if (match.getTypeMatch() == TypeMatch.PUBLIC) {
            throw new BusinessException("Match is already public");
        }

        match.setTypeMatch(TypeMatch.PUBLIC);
        match.setDateConversionPublic(java.time.LocalDateTime.now());
        matchRepository.save(match);

        // pénalité pour l'organisateur
        membreService.addPenalty(match.getOrganisateur().getId());

        log.info("Match {} converted to public, penalty applied to organizer {}",
                matchId, match.getOrganisateur().getId());
    }

    @Override
    @Transactional
    public void checkAndConvertExpiredPrivateMatches() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Match> expiredMatches = matchRepository
                .findByDateAndStatut(tomorrow, StatutMatch.PLANIFIE)
                .stream()
                .filter(m -> m.getTypeMatch() == TypeMatch.PRIVE)
                .filter(m -> m.getNbJoueursActuels() < MAX_PLAYERS)
                .toList();

        expiredMatches.forEach(m -> convertToPublic(m.getId()));

        log.info("Scheduler : {} private match(es) converted to public", expiredMatches.size());
    }

    @Override
    @Transactional
    public void incrementPlayers(Long matchId) {
        Match match = getById(matchId);

        if (match.getNbJoueursActuels() >= MAX_PLAYERS) {
            throw new BusinessException("Match is already full");
        }

        match.setNbJoueursActuels(match.getNbJoueursActuels() + 1);

        if (match.getNbJoueursActuels() == MAX_PLAYERS) {
            match.setStatut(StatutMatch.COMPLET);
        }

        matchRepository.save(match);
    }

    @Override
    @Transactional
    public void decrementPlayers(Long matchId) {
        Match match = getById(matchId);

        if (match.getNbJoueursActuels() <= 0) {
            throw new BusinessException("Match already has 0 players");
        }

        match.setNbJoueursActuels(match.getNbJoueursActuels() - 1);
        match.setStatut(StatutMatch.PLANIFIE);
        matchRepository.save(match);
    }

    @Override
    public boolean isMatchFull(Long matchId) {
        Match match = getById(matchId);
        return match.getNbJoueursActuels() >= MAX_PLAYERS;
    }

    @Override
    public boolean isSlotAvailable(Long terrainId, LocalDate date) {
        List<Match> existing = matchRepository.findByTerrainSiteId(terrainId)
                .stream()
                .filter(m -> m.getDate().equals(date))
                .filter(m -> m.getStatut() != StatutMatch.ANNULE)
                .toList();
        return existing.isEmpty();
    }

    private void validateBookingDelay(Membre membre, LocalDate matchDate) {
        LocalDate today = LocalDate.now();
        long daysUntilMatch = today.until(matchDate).getDays();

        int requiredDays = switch (membre.getTypeMembre()) {
            case GLOBAL -> 21;  // 3 semaines
            case SITE   -> 14;  // 2 semaines
            case LIBRE  -> 5;   // 5 jours
        };

        if (daysUntilMatch < requiredDays) {
            throw new BusinessException(
                    "Member type " + membre.getTypeMembre() +
                            " must book at least " + requiredDays + " days in advance"
            );
        }
    }

    private void validateSiteNotClosed(Terrain terrain, LocalDate date) {

        if (terrain.getSite().getJoursFermeture() == null) {
            throw new BusinessException("Site has no closed days");
        }

        boolean isClosed = terrain.getSite().getJoursFermeture()
                .stream()
                .anyMatch(j -> j.getDate().equals(date));

        if (isClosed) {
            throw new BusinessException("The site is closed on : " + date);
        }
    }
}
