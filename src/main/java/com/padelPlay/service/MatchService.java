package com.padelPlay.service;

import com.padelPlay.entity.Match;
import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto;

import java.util.List;

/**
 * Service pour la gestion des matchs.
 * Fournit les opérations métier liées aux matchs, telles que la création,
 * la recherche et la gestion des joueurs.
 */
public interface MatchService {

    /**
     * Crée un nouveau match en appliquant toutes les règles métier.
     *
     * @param request Les données de création du match.
     * @param username Le nom d'utilisateur (matricule) de l'organisateur.
     * @return Le DTO du match créé.
     */
    MatchDto createMatch(CreateMatchRequest request, String username);

    /**
     * Récupère tous les matchs.
     *
     * @return Une liste de tous les matchs sous forme de DTOs.
     */
    List<MatchDto> findAllMatches();

    /**
     * Récupère tous les matchs publics et disponibles.
     *
     * @return Une liste de DTOs des matchs publics et planifiés.
     */
    List<MatchDto> getPublicAvailableMatches();

    /**
     * Trouve un match par son identifiant.
     *
     * @param id L'identifiant du match.
     * @return L'entité Match correspondante.
     * @throws com.padelPlay.exception.ResourceNotFoundException si le match n'est pas trouvé.
     */
    Match getById(Long id);

    /**
     * Incrémente le nombre de joueurs pour un match.
     *
     * @param matchId L'identifiant du match.
     */
    void incrementPlayers(Long matchId);

    /**
     * Décrémente le nombre de joueurs pour un match.
     *
     * @param matchId L'identifiant du match.
     */
    void decrementPlayers(Long matchId);

    /**
     * Tâche planifiée pour vérifier et convertir les matchs privés expirés en matchs publics.
     */
    void checkAndConvertExpiredPrivateMatches();
}
