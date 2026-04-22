package com.padelPlay.controller;

import com.padelPlay.dto.request.SiteRequest;
import com.padelPlay.dto.response.SiteResponse;
import com.padelPlay.entity.Site;
import com.padelPlay.mapper.SiteMapper;
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
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@Tag(name = "Sites", description = "Endpoints de gestion des sites de padel. Un site contient plusieurs terrains et définit ses propres horaires d'ouverture, durée de match et jours de fermeture.")
public class SiteController {

    private final SiteService siteService;
    private final SiteMapper siteMapper;

    @Operation(
            summary = "Créer un nouveau site",
            description = "Crée un nouveau site de padel avec sa configuration (horaires d'ouverture, durée de match, durée de pause). Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Site créé avec succès",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide ou erreur de validation",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody SiteRequest request) {
        Site site = siteMapper.toEntity(request);
        Site saved = siteService.create(site);
        return ResponseEntity.status(HttpStatus.CREATED).body(siteMapper.toResponse(saved));
    }

    @Operation(
            summary = "Obtenir tous les sites",
            description = "Retourne la liste de tous les sites de padel. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des sites retournée avec succès",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<SiteResponse>> getAll() {
        List<SiteResponse> sites = siteService.getAll()
                .stream()
                .map(siteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(sites);
    }

    @Operation(
            summary = "Obtenir un site par ID",
            description = "Retourne un site unique par son ID. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Site trouvé et retourné",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Site introuvable",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getById(
            @Parameter(description = "ID du site à récupérer", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(siteMapper.toResponse(siteService.getById(id)));
    }

    @Operation(
            summary = "Mettre à jour un site",
            description = "Met à jour tous les champs d'un site existant. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Site mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = SiteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide ou erreur de validation",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Site introuvable",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> update(
            @Parameter(description = "ID du site à mettre à jour", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {
        Site site = siteMapper.toEntity(request);
        Site updated = siteService.update(id, site);
        return ResponseEntity.ok(siteMapper.toResponse(updated));
    }

    @Operation(
            summary = "Supprimer un site",
            description = "Supprime définitivement un site et tous ses terrains et jours de fermeture associés. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Site supprimé avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Site introuvable",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID du site à supprimer", required = true)
            @PathVariable Long id) {
        siteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
