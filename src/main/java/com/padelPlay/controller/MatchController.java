package com.padelPlay.controller;

import com.padelPlay.dto.request.MatchRequest;
import com.padelPlay.dto.response.MatchResponse;
import com.padelPlay.entity.Match;
import com.padelPlay.mapper.MatchMapper;
import com.padelPlay.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Endpoints for managing padel matches. " +
        "A match is created on a specific court for a specific date and time slot. " +
        "Matches can be PRIVATE (organizer adds players manually, 4 players required) " +
        "or PUBLIC (any member can join by paying). " +
        "Business rules: " +
        "- Each match costs 60€ split across 4 players (15€ each). " +
        "- A private match with less than 4 players the day before automatically becomes public. " +
        "- The organizer receives a 7-day penalty if their private match is not filled. " +
        "- Booking delays vary by member type: GLOBAL = 3 weeks, SITE = 2 weeks, LIBRE = 5 days.")
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @Operation(
            summary = "Create a new match",
            description = "Creates a new match on a specific court. " +
                    "The organizer is automatically registered as the first player (nbJoueursActuels = 1). " +
                    "Business rules enforced at creation: " +
                    "1. The organizer must not have an active penalty. " +
                    "2. The organizer must not have an outstanding balance. " +
                    "3. The booking delay must be respected according to the organizer's member type. " +
                    "4. The court must be available on the requested date. " +
                    "5. The site must not be closed on the requested date. " +
                    "The end time is automatically calculated from the site's match duration configuration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Match successfully created",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Business rule violation — penalty, balance, booking delay, slot already taken, or site closed",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Organizer or court not found",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody MatchRequest request) {
        Match match = matchMapper.toEntity(request);
        Match saved = matchService.create(match, request.getOrganisateurId(), request.getTerrainId());
        return ResponseEntity.status(HttpStatus.CREATED).body(matchMapper.toResponse(saved));
    }

    @Operation(
            summary = "Get all matches",
            description = "Returns the complete list of all matches regardless of type or status. " +
                    "Includes private, public, planned, complete, and cancelled matches. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all matches returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MatchResponse>> getAll() {
        List<MatchResponse> matches = matchService.getAll()
                .stream()
                .map(matchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(matches);
    }

    @Operation(
            summary = "Get a match by ID",
            description = "Returns a single match by its ID. " +
                    "Includes full details: court, site, organizer, date, time slot, type, status, " +
                    "current number of players, price per player, and public conversion date if applicable. " +
                    "Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match found and returned",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getById(
            @Parameter(description = "ID of the match to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(matchMapper.toResponse(matchService.getById(id)));
    }

    @Operation(
            summary = "Get all available public matches",
            description = "Returns all public matches with status PLANIFIE that still have open spots. " +
                    "This is the main endpoint used by the member interface to display joinable matches. " +
                    "A match appears here either because it was created as public, " +
                    "or because it was a private match that was automatically converted to public " +
                    "the day before due to insufficient players. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of available public matches returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class)))
    })
    @GetMapping("/public")
    public ResponseEntity<List<MatchResponse>> getPublicAvailable() {
        List<MatchResponse> matches = matchService.getPublicAvailableMatches()
                .stream()
                .map(matchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(matches);
    }

    @Operation(
            summary = "Get all matches by site",
            description = "Returns all matches hosted on courts belonging to a specific site, " +
                    "regardless of match type or status. " +
                    "Used by the admin interface to monitor activity per site. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matches for the given site returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Site not found",
                    content = @Content)
    })
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<MatchResponse>> getBySiteId(
            @Parameter(description = "ID of the site to retrieve matches for", required = true)
            @PathVariable Long siteId) {
        List<MatchResponse> matches = matchService.getBySiteId(siteId)
                .stream()
                .map(matchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(matches);
    }

    @Operation(
            summary = "Get all matches organized by a member",
            description = "Returns all matches where the given member is the organizer. " +
                    "Useful for displaying a member's match history and upcoming organized matches " +
                    "in the member interface. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matches organized by the member returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<MatchResponse>> getByOrganisateur(
            @Parameter(description = "ID of the organizer member", required = true)
            @PathVariable Long organisateurId) {
        List<MatchResponse> matches = matchService.getByOrganisateurId(organisateurId)
                .stream()
                .map(matchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(matches);
    }

    @Operation(
            summary = "Manually convert a private match to public",
            description = "Forces a private match to become public before the automatic scheduler runs. " +
                    "This can be triggered manually by an admin. " +
                    "When converted: the match becomes visible to all members, " +
                    "the organizer automatically receives a 7-day booking penalty, " +
                    "and the conversion timestamp is recorded. " +
                    "The match must currently be of type PRIVE to be converted. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Match successfully converted to public"),
            @ApiResponse(responseCode = "400", description = "Match is already public",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied — admin token required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Match not found",
                    content = @Content)
    })
    @PatchMapping("/{id}/convert-public")
    public ResponseEntity<Void> convertToPublic(
            @Parameter(description = "ID of the private match to convert to public", required = true)
            @PathVariable Long id) {
        matchService.convertToPublic(id);
        return ResponseEntity.noContent().build();
    }
}
