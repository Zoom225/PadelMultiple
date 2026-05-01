package com.padelPlay.controller;

import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto;
import com.padelPlay.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {
    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchDto> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Requête de création de match reçue de l'utilisateur '{}' pour le terrain ID {}", username, request.terrainId());

        // La sécurité Spring gère déjà le cas de l'utilisateur anonyme,
        // mais une vérification explicite peut être conservée pour des logs plus clairs.
        if (username == null || "anonymousUser".equals(username)) {
            log.warn("Tentative de création de match par un utilisateur non authentifié.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        MatchDto createdMatch = matchService.createMatch(request, username);
        return new ResponseEntity<>(createdMatch, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MatchDto>> getAllMatches() {
        return ResponseEntity.ok(matchService.findAllMatches());
    }

    @GetMapping("/public")
    public ResponseEntity<List<MatchDto>> getPublicMatches() {
        // Correction : Le service retourne maintenant directement une List<MatchDto>.
        // La conversion manuelle et la dépendance vers MatchMapper ne sont plus nécessaires ici.
        List<MatchDto> matches = matchService.getPublicAvailableMatches();
        return ResponseEntity.ok(matches);
    }
}
