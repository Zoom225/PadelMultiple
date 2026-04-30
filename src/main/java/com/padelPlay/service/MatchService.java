package com.padelPlay.service;

import com.padelPlay.entity.Match;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Terrain;
import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import com.padelPlay.exception.MatchCreationException;
import com.padelPlay.mapper.MatchMapper;
import com.padelPlay.match.dto.CreateMatchRequest;
import com.padelPlay.match.dto.MatchDto; // Chemin d'import corrigé
import com.padelPlay.repository.MatchRepository;
import com.padelPlay.repository.MembreRepository;
import com.padelPlay.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import inutile supprimé
import java.time.LocalDateTime;
import java.util.List;

public interface MatchService {
    com.padelPlay.match.dto.MatchDto createMatch(com.padelPlay.match.dto.CreateMatchRequest request, String username);
    java.util.List<com.padelPlay.match.dto.MatchDto> findAllMatches();
    void checkAndConvertExpiredPrivateMatches();
    Match getById(Long id);
    void incrementPlayers(Long matchId);
    void decrementPlayers(Long matchId);
}
