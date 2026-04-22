package com.padelPlay.controller;

import com.padelPlay.dto.request.MembreRequest;
import com.padelPlay.dto.response.MembreResponse;
import com.padelPlay.entity.Membre;
import com.padelPlay.entity.Site;
import com.padelPlay.mapper.MembreMapper;
import com.padelPlay.service.MembreService;
import com.padelPlay.service.SiteService;
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
@RequestMapping("/api/membres")
@RequiredArgsConstructor
@Tag(name = "Membres", description = "Interfaces de gestion des membres de padel. " +
        "Il existe 3 types d'adhérents : GLOBAL (matricule commençant par G, réservation 3 semaines à l'avance, tous les sites), \" +\n" +
        "SITE (matricule commençant par S, réservation 2 semaines à l'avance, leur site uniquement)," +
        "LIBRE (matricule commençant par L, réservation 5 jours à l'avance, tous les sites)." +
        "L'authentification se fait uniquement via le matricule — aucun mot de passe n'est requis pour les membres.")
public class MembreController {

    private final MembreService membreService;
    private final SiteService siteService;
    private final MembreMapper membreMapper;

    @Operation(
            summary = "Inscrire un nouveau membre",
            description = "Crée un nouveau membre avec un matricule unique." +
                    "Le format du matricule est validé en fonction du type de membre :" +
                    " GLOBAL → G suivi de 4 chiffres (par ex. G1234)," +
                    " SITE → S suivi de 5 chiffres (par ex. S12345)," +
                    "LIBRE → L suivi de 5 chiffres (par ex. L12345)." +
                    "Un membre SITE doit fournir un siteId. Les membres GLOBAL et LIBRE n'en ont pas besoin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member successfully registered",
                    content = @Content(schema = @Schema(implementation = MembreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body, wrong matricule format, or matricule already exists",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Site not found (when siteId is provided)",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<MembreResponse> create(@Valid @RequestBody MembreRequest request) {
        Membre membre = membreMapper.toEntity(request);

        if (request.getSiteId() != null) {
            Site site = siteService.getById(request.getSiteId());
            membre.setSite(site);
        }

        Membre saved = membreService.create(membre);
        return ResponseEntity.status(HttpStatus.CREATED).body(membreMapper.toResponse(saved));
    }

    @Operation(
            summary = "Get all members",
            description = "Returns the list of all registered members across all sites. Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of members returned successfully",
                    content = @Content(schema = @Schema(implementation = MembreResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MembreResponse>> getAll() {
        List<MembreResponse> membres = membreService.getAll()
                .stream()
                .map(membreMapper::toResponse)
                .toList();
        return ResponseEntity.ok(membres);
    }

    @Operation(
            summary = "Rechercher un membre par identifiant",
            description = "Renvoie un membre donné en fonction de son identifiant interne." +
                    "Comprend le type de membre, les informations relatives au site et le solde impayé actuel. Accessible au public."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found and returned",
                    content = @Content(schema = @Schema(implementation = MembreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MembreResponse> getById(
            @Parameter(description = "Internal ID of the member", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(membreMapper.toResponse(membreService.getById(id)));
    }

    @Operation(
            summary = "Rechercher un membre par matricule",
            description = "Récupère un membre à l'aide de son matricule unique." +
                    "Il s'agit du principal moyen d'identifier un membre, car l'authentification s'effectue uniquement via le matricule. Accessible au public via le matricule."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found and returned",
                    content = @Content(schema = @Schema(implementation = MembreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found with given matricule",
                    content = @Content)
    })
    @GetMapping("/matricule/{matricule}")
    public ResponseEntity<MembreResponse> getByMatricule(
            @Parameter(description = "Unique matricule of the member (e.g. G1234, S12345, L12345)", required = true)
            @PathVariable String matricule) {
        return ResponseEntity.ok(membreMapper.toResponse(membreService.getByMatricule(matricule)));
    }

    @Operation(
            summary = "Vérifier si un membre fait l'objet d'une sanction en cours",
            description = "Renvoie « vrai » si le membre fait actuellement l'objet d'une sanction." +
                    "Une sanction est appliquée lorsqu'un membre organise un match privé qui n'est pas complet 24 heures avant la date du match. +\n" +
                    "Pendant la durée de la sanction (7 jours), le membre ne peut ni créer ni rejoindre de match. Accessible au public. "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Penalty status returned",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @GetMapping("/{id}/penalty")
    public ResponseEntity<Boolean> hasActivePenalty(
            @Parameter(description = "ID of the member to check", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(membreService.hasActivePenalty(id));
    }

    @Operation(
            summary = "Vérifier si un membre a un solde impayé",
            description = "Renvoie « true » si le membre a un solde impayé." +
                    "Un solde impayé est généré lorsqu'un membre organise une partie publique qui n'est pas complète — " +
                    "l'organisateur doit alors prendre en charge la part des joueurs manquants." +
                    "Un membre ayant un solde impayé ne peut pas créer de nouvelle partie tant que ce solde n'est pas réglé." +
                    "Le solde est automatiquement déduit lors du prochain paiement. Accessible au public."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance status returned",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @GetMapping("/{id}/balance")
    public ResponseEntity<Boolean> hasOutstandingBalance(
            @Parameter(description = "ID of the member to check", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(membreService.hasOutstandingBalance(id));
    }

    @Operation(
            summary = "Mettre à jour un membre",
            description = "Met à jour les informations personnelles d'un membre existant (nom, prénom, adresse e-mail)." +
                    "Le matricule et le type de membre ne peuvent pas être modifiés après l'inscription. Nécessite le rôle ADMIN. ",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member successfully updated",
                    content = @Content(schema = @Schema(implementation = MembreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied — admin token required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MembreResponse> update(
            @Parameter(description = "ID of the member to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody MembreRequest request) {
        Membre membre = membreMapper.toEntity(request);
        Membre updated = membreService.update(id, membre);
        return ResponseEntity.ok(membreMapper.toResponse(updated));
    }

    @Operation(
            summary = "Delete a member",
            description = "Supprime définitivement un membre ainsi que toutes ses réservations et pénalités associées.  +\n" +
                    " Cette action est irréversible. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied — admin token required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the member to delete", required = true)
            @PathVariable Long id) {
        membreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
