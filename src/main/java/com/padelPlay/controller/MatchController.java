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
@Tag(name = "Matches", description = "Fonctionnalités pour la gestion des matchs de padel." +
        "Un match est créé sur un terrain spécifique, pour une date et un créneau horaire précis." +
        "Les matchs peuvent être PRIVÉS (l'organisateur ajoute les joueurs manuellement, 4 joueurs requis)" +
        "ou PUBLICS (tout membre peut s'inscrire moyennant paiement)." +
        "Règles de fonctionnement : " +
        "- Chaque match coûte 60 €, répartis entre les 4 joueurs (15 € chacun)." +
        "- Un match privé comptant moins de 4 joueurs la veille devient automatiquement public." +
        "- L'organisateur se voit infliger une pénalité de 7 jours si son match privé n'est pas complet." +
        "- Les délais de réservation varient selon le type de membre : GLOBAL = 3 semaines, SITE = 2 semaines, LIBRE = 5 jours.")
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @Operation(
            summary = "Create a new match",
               description = "Crée un nouveau match sur un terrain spécifique." + "L'organisateur est automatiquement enregistré comme premier joueur (nbJoueursActuels = 1)." +
                    "Règles de gestion appliquées lors de la création :" +
                    "1. L'organisateur ne doit pas avoir de sanction en cours." +
                    "2. L'organisateur ne doit pas avoir de solde impayé." +
                    "3. Le délai de réservation doit être respecté en fonction du type d'adhésion de l'organisateur." +
                    "4. Le terrain doit être disponible à la date demandée." +
                    "5. Le site ne doit pas être fermé à la date demandée." +
                    "L'heure de fin est automatiquement calculée à partir de la configuration de la durée des matchs du site."
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
            summary = "Rechercher un élément par ID",
            description = "Renvoie un seul résultat correspondant à son identifiant.  +\n" +
                    "Comprend toutes les informations : terrain, site, organisateur, date, créneau horaire, type, statut, +\n" +
                    "nombre actuel de joueurs, prix par joueur et date de conversion publique, le cas échéant.  +\n" +
                    "Accessible au public. "
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
            summary = "Afficher tous les matchs publics disponibles",
            description = "Renvoie toutes les rencontres publiques ayant le statut PLANIFIÉ et pour lesquelles il reste des places disponibles." +
                    "Il s'agit du point de terminaison principal utilisé par l'interface des membres pour afficher les rencontres auxquelles il est possible de s'inscrire. +\n" +
                    "Une rencontre apparaît ici soit parce qu'elle a été créée en tant que rencontre publique," +
                    "soit parce qu'il s'agissait d'une rencontre privée qui a été automatiquement convertie en rencontre publique" +
                    "la veille en raison d'un nombre insuffisant de joueurs. Accessible au public."
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
            summary = "Afficher tous les résultats par site",
            description = "Renvoie tous les matchs disputés sur les terrains d'un site donné," +
                    "quel que soit leur type ou leur statut."+
                    "Utilisé par l'interface d'administration pour suivre l'activité par site. Accessible au public."
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
            summary = "Afficher tous les matchs organisés par un membre",
            description = "Renvoie tous les matchs dont le membre indiqué est l'organisateur." +
                    "Utile pour afficher l'historique des matchs d'un membre et les prochains matchs qu'il organise" +
                    "dans l'interface membre. Accessible au public."
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
            summary = "Convertir manuellement un match privé en match public",
            description = "Force un match privé à devenir public avant l'exécution du planificateur automatique." +
                    "Cette opération peut être déclenchée manuellement par un administrateur." +
                    "Une fois converti : le match devient visible pour tous les membres," +
                    "l'organisateur se voit automatiquement infliger une pénalité de réservation de 7 jours," +
                    "et l'horodatage de la conversion est enregistré." +
                    "Le match doit actuellement être de type PRIVE pour pouvoir être converti. Nécessite le rôle ADMIN.",
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
