package com.padelPlay.service.impl;

import com.padelPlay.entity.Match;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Terrain;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.entity.enums.TypeMembre;
import com.padelPlay.exception.BusinessException;
import com.padelPlay.exception.ResourceNotFoundException;
import com.padelPlay.mapper.MatchMapper;
import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto;
import com.padelPlay.repository.MatchRepository;
import com.padelPlay.repository.MembreRepository;
import com.padelPlay.service.MatchService;
import com.padelPlay.service.MembreService;
import com.padelPlay.service.TerrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    // Dépendances uniques et finales gérées par Lombok
    private final MatchRepository matchRepository;
    private final MembreRepository membreRepository; // Nécessaire pour findByMatricule
    private final TerrainService terrainService;
    private final MembreService membreService;
    private final MatchMapper matchMapper;

    private static final int MAX_PLAYERS = 4;

    @Override
    @Transactional
    public MatchDto createMatch(CreateMatchRequest request, String username) {
        // 1. Récupération des entités
        Membre organisateur = membreRepository.findByMatricule(username)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé pour l'utilisateur: " + username));

        Terrain terrain = terrainService.getById(request.terrainId());

        // 2. Validation des règles métier
        if (membreService.hasOutstandingBalance(organisateur.getId())) {
            throw new BusinessException("Le membre a un solde impayé et ne peut pas créer de match.");
        }
        if (membreService.hasActivePenalty(organisateur.getId())) {
            throw new BusinessException("Le membre a une pénalité active et ne peut pas créer de match.");
        }
        validateBookingDelay(organisateur, request.matchDate().toLocalDate());

        // 3. Calcul des dates et validation du créneau
        LocalDateTime dateDebut = request.matchDate();
        LocalDateTime dateFin = dateDebut.plusMinutes(terrain.getSite().getDureeMatchMinutes());

        validateSiteNotClosed(terrain, dateDebut.toLocalDate());
        validateSiteOpeningHours(terrain, dateDebut.toLocalTime(), dateFin.toLocalTime());

        if (!isSlotAvailable(terrain.getId(), dateDebut, dateFin)) {
            throw new BusinessException("Ce créneau est déjà réservé sur le terrain : " + terrain.getId());
        }

        // 4. Création de l'entité Match
        Match match = new Match();
        match.setOrganisateur(organisateur);
        match.setTerrain(terrain);
        match.setDateDebut(dateDebut);
        match.setDateFin(dateFin);
        match.setTypeMatch(TypeMatch.valueOf(request.matchType()));
        match.setStatut(StatutMatch.PLANIFIE);
        match.setNbJoueursActuels(1); // L'organisateur est le premier joueur
        match.setPrixTotal(terrain.getPrix());
        match.setPrixParJoueur(terrain.getPrix() / MAX_PLAYERS);

        Match savedMatch = matchRepository.save(match);
        log.info("Match créé avec succès (ID: {}) par l'utilisateur {}", savedMatch.getId(), username);

        return matchMapper.toMatchDto(savedMatch);
    }

    @Override
    public List<MatchDto> findAllMatches() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toMatchDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDto> getPublicAvailableMatches() {
        return matchRepository.findByTypeMatchAndStatut(TypeMatch.PUBLIC, StatutMatch.PLANIFIE)
                .stream()
                .map(matchMapper::toMatchDto)
                .collect(Collectors.toList());
    }

    @Override
    public Match getById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match non trouvé avec l'ID : " + id));
    }

    @Override
    @Transactional
    public void checkAndConvertExpiredPrivateMatches() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = tomorrow.atStartOfDay();
        LocalDateTime endOfDay = tomorrow.atTime(LocalTime.MAX);

        List<Match> expiredMatches = matchRepository
                .findByDateDebutBetweenAndStatut(startOfDay, endOfDay, StatutMatch.PLANIFIE)
                .stream()
                .filter(m -> m.getTypeMatch() == TypeMatch.PRIVE && m.getNbJoueursActuels() < MAX_PLAYERS)
                .toList();

        if (!expiredMatches.isEmpty()) {
            expiredMatches.forEach(m -> convertToPublic(m.getId()));
            log.info("Scheduler : {} match(s) privé(s) ont été converti(s) en public", expiredMatches.size());
        }
    }

    @Transactional
    public void convertToPublic(Long matchId) {
        Match match = getById(matchId);
        if (match.getTypeMatch() == TypeMatch.PUBLIC) {
            throw new BusinessException("Le match est déjà public.");
        }
        match.setTypeMatch(TypeMatch.PUBLIC);
        match.setDateConversionPublic(LocalDateTime.now());
        matchRepository.save(match);
        membreService.addPenalty(match.getOrganisateur().getId());
        log.info("Match {} converti en public, pénalité appliquée à l'organisateur {}", matchId, match.getOrganisateur().getId());
    }

    @Override
    @Transactional
    public void incrementPlayers(Long matchId) {
        Match match = getById(matchId);
        if (match.getNbJoueursActuels() >= MAX_PLAYERS) {
            throw new BusinessException("Le match est déjà complet.");
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
            throw new BusinessException("Le match n'a aucun joueur.");
        }
        match.setNbJoueursActuels(match.getNbJoueursActuels() - 1);
        if (match.getStatut() == StatutMatch.COMPLET) {
            match.setStatut(StatutMatch.PLANIFIE);
        }
        matchRepository.save(match);
    }

    private boolean isSlotAvailable(Long terrainId, LocalDateTime start, LocalDateTime end) {
        List<Match> existingMatches = matchRepository.findOverlappingMatches(terrainId, start, end, StatutMatch.ANNULE);
        return existingMatches.isEmpty();
    }

    private void validateBookingDelay(Membre membre, LocalDate matchDate) {
        long daysUntilMatch = LocalDate.now().until(matchDate).getDays();
        int requiredDays;
        switch (membre.getTypeMembre()) {
            case GLOBAL: requiredDays = 21; break;
            case SITE: requiredDays = 14; break;
            case LIBRE: requiredDays = 5; break;
            default: throw new IllegalStateException("Type de membre inconnu: " + membre.getTypeMembre());
        }
        if (daysUntilMatch < requiredDays) {
            throw new BusinessException("Le type de membre " + membre.getTypeMembre() + " doit réserver au moins " + requiredDays + " jours à l'avance.");
        }
    }

    private void validateSiteNotClosed(Terrain terrain, LocalDate date) {
        if (terrain.getSite().getJoursFermeture() != null &&
                terrain.getSite().getJoursFermeture().stream().anyMatch(j -> j.getDate().equals(date))) {
            throw new BusinessException("Le site est fermé à la date : " + date);
        }
    }

    private void validateSiteOpeningHours(Terrain terrain, LocalTime heureDebut, LocalTime heureFin) {
        LocalTime openingTime = terrain.getSite().getHeureOuverture();
        LocalTime closingTime = terrain.getSite().getHeureFermeture();
        if (heureDebut.isBefore(openingTime) || heureFin.isAfter(closingTime)) {
            throw new BusinessException("Le match est en dehors des heures d'ouverture du site (" + openingTime + " - " + closingTime + ").");
        }
    }
}
