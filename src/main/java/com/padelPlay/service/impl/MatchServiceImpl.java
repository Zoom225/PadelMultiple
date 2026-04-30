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
public class MatchServiceImpl implements com.padelPlay.service.MatchService {
    private final com.padelPlay.mapper.MatchMapper matchMapper;
    private final com.padelPlay.repository.MembreRepository membreRepository;
    private final com.padelPlay.repository.TerrainRepository terrainRepository;
    @org.springframework.transaction.annotation.Transactional
    public com.padelPlay.match.dto.MatchDto createMatch(com.padelPlay.match.dto.CreateMatchRequest request, String username) {
        Membre organisateur = membreRepository.findByMatricule(username)
                .orElseThrow(() -> new IllegalStateException("Membre authentifié non trouvé : " + username));

        Terrain terrain = terrainRepository.findById(request.terrainId())
                .orElseThrow(() -> new com.padelPlay.exception.MatchCreationException("Terrain non trouvé avec l'ID : " + request.terrainId()));

        // Règle de réservation
        validateBookingDelay(organisateur, request.matchDate().toLocalDate());

        java.time.LocalDateTime dateDebut = request.matchDate();
        java.time.LocalDateTime dateFin = dateDebut.plusMinutes(90);
        double prixTotal = terrain.getPrix();
        double prixParJoueur = prixTotal / 4;

        Match match = new Match();
        match.setTerrain(terrain);
        match.setOrganisateur(organisateur);
        match.setDateDebut(dateDebut);
        match.setDateFin(dateFin);
        match.setTypeMatch(com.padelPlay.entity.enums.TypeMatch.valueOf(request.matchType()));
        match.setStatut(com.padelPlay.entity.enums.StatutMatch.PLANIFIE);
        match.setNbJoueursActuels(0);
        match.setPrixTotal(prixTotal);
        match.setPrixParJoueur(prixParJoueur);

        Match savedMatch = matchRepository.save(match);
        log.info("Match créé avec succès avec l'ID {} par l'utilisateur {}", savedMatch.getId(), username);

        return matchMapper.toMatchDto(savedMatch);
    }

    public java.util.List<com.padelPlay.match.dto.MatchDto> findAllMatches() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toMatchDto)
                .toList();
    }

    private static final int MAX_PLAYERS     = 4;
    private static final double MATCH_PRICE  = 60.0;

    private final MatchRepository matchRepository;
    private final MembreService membreService;
    private final TerrainService terrainService;

    // Implémentation de la méthode abstraite attendue par l'interface
    public boolean isSlotAvailable(Long terrainId, LocalDate date) {
        // On considère toute la journée pour détecter tout chevauchement
        LocalTime debut = LocalTime.MIN;
        LocalTime fin = LocalTime.MAX;
        return isSlotAvailable(terrainId, date, debut, fin);
    }

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
        validateBookingDelay(organisateur, match.getDateDebut().toLocalDate());

        // calcul des heures de fin selon la config du site
        LocalTime heureFin = match.getDateDebut().toLocalTime()
                .plusMinutes(terrain.getSite().getDureeMatchMinutes());
        match.setDateFin(java.time.LocalDateTime.of(match.getDateDebut().toLocalDate(), heureFin));

        // règle : vérifier que le créneau est disponible sur ce terrain
        if (!isSlotAvailable(terrainId, match.getDateDebut().toLocalDate(), match.getDateDebut().toLocalTime(), heureFin)) {
            throw new BusinessException("This slot is already booked on terrain : " + terrainId);
        }

        // règle : vérifier que le site n'est pas fermé ce jour là
        validateSiteNotClosed(terrain, match.getDateDebut().toLocalDate());
        // règle : vérifier que le créneau est dans les heures d'ouverture du site
        validateSiteOpeningHours(terrain, match.getDateDebut().toLocalTime(), heureFin);

        match.setOrganisateur(organisateur);
        match.setTerrain(terrain);
        match.setNbJoueursActuels(1);
        match.setPrixTotal(MATCH_PRICE);
        match.setPrixParJoueur(MATCH_PRICE / MAX_PLAYERS);
        match.setStatut(StatutMatch.PLANIFIE);
        // NE PAS FORCER LE TYPE, respecter la valeur passée (PUBLIC ou PRIVE)
        // match.setTypeMatch(TypeMatch.PRIVE); // SUPPRIMÉ

        log.info("Match created by member {} on terrain {} at {}",
                organisateurId, terrainId, match.getDateDebut());

        return matchRepository.save(match);
    }

    public Match getById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id : " + id));
    }

    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    public List<Match> getPublicAvailableMatches() {
        return matchRepository.findByTypeMatchAndStatut(
                TypeMatch.PUBLIC,
                StatutMatch.PLANIFIE
        );
    }

    public List<Match> getBySiteId(Long siteId) {
        return matchRepository.findByTerrainSiteId(siteId);
    }

    public List<Match> getByOrganisateurId(Long organisateurId) {
        return matchRepository.findByOrganisateurId(organisateurId);
    }

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

    @Transactional
    public void checkAndConvertExpiredPrivateMatches() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        java.time.LocalDateTime start = tomorrow.atStartOfDay();
        java.time.LocalDateTime end = tomorrow.atTime(java.time.LocalTime.MAX);

        List<Match> expiredMatches = matchRepository
                .findByDateDebutBetweenAndStatut(start, end, StatutMatch.PLANIFIE)
                .stream()
                .filter(m -> m.getTypeMatch() == TypeMatch.PRIVE)
                .filter(m -> m.getNbJoueursActuels() < MAX_PLAYERS)
                .toList();

        expiredMatches.forEach(m -> convertToPublic(m.getId()));

        log.info("Scheduler : {} private match(es) converted to public", expiredMatches.size());
    }

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

    public boolean isMatchFull(Long matchId) {
        Match match = getById(matchId);
        return match.getNbJoueursActuels() >= MAX_PLAYERS;
    }

    public boolean isSlotAvailable(Long terrainId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.atTime(java.time.LocalTime.MAX);
        List<Match> existingMatches = matchRepository.findByTerrainIdAndDateDebutBetweenAndStatutNot(terrainId, start, end, StatutMatch.ANNULE);

        for (Match existingMatch : existingMatches) {
            LocalTime existingStart = existingMatch.getDateDebut().toLocalTime();
            LocalTime existingEnd = existingMatch.getDateFin().toLocalTime();

            // Check for overlap
            if (heureDebut.isBefore(existingEnd) && heureFin.isAfter(existingStart)) {
                return false; // Overlap found
            }
        }
        return true; // No overlap
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
        if (terrain.getSite().getJoursFermeture() != null) {
            boolean isClosed = terrain.getSite().getJoursFermeture()
                    .stream()
                    .anyMatch(j -> j.getDate().equals(date));

            if (isClosed) {
                throw new BusinessException("The site is closed on : " + date);
            }
        }
    }
    
    private void validateSiteOpeningHours(Terrain terrain, LocalTime heureDebut, LocalTime heureFin) {
        LocalTime openingTime = terrain.getSite().getHeureOuverture();
        LocalTime closingTime = terrain.getSite().getHeureFermeture();

        if (heureDebut.isBefore(openingTime) || heureFin.isAfter(closingTime)) {
            throw new BusinessException("The match is outside site opening hours");
        }
    }

    @Transactional
    public void deleteById(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found with id : " + matchId);
        }
        matchRepository.deleteById(matchId);
    }
}
