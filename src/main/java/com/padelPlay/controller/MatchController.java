package com.padelPlay.controller;

import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto; // Chemin d'import corrigé
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

        MatchDto createdMatch = matchService.createMatch(request, username);
        return new ResponseEntity<>(createdMatch, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MatchDto>> getAllMatches() {
        return ResponseEntity.ok(matchService.findAllMatches());
    }
}
