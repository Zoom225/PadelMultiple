package com.padelPlay.controller;

import com.padelPlay.dto.request.MatchRequest;
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
        return ResponseEntity.ok(matchService.getPublicAvailableMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchDtoById(id));
    }

    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<MatchDto>> getByOrganisateur(@PathVariable Long organisateurId) {
        return ResponseEntity.ok(matchService.findByOrganisateur(organisateurId));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<MatchDto>> getBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(matchService.findBySite(siteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDto> updateMatch(
            @PathVariable Long id,
            @Valid @RequestBody MatchRequest request) {
        return ResponseEntity.ok(matchService.updateMatch(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelMatch(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
        matchService.cancelMatch(id, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/convert-public")
    public ResponseEntity<Void> convertToPublic(@PathVariable Long id) {
        matchService.convertToPublic(id);
        return ResponseEntity.noContent().build();
    }
}