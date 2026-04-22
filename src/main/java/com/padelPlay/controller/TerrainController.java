package com.padelPlay.controller;

import com.padelPlay.dto.request.TerrainRequest;
import com.padelPlay.dto.response.TerrainResponse;
import com.padelPlay.entity.Terrain;
import com.padelPlay.mapper.TerrainMapper;
import com.padelPlay.service.TerrainService;
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
@RequestMapping("/api/terrains")
@RequiredArgsConstructor
@Tag(name = "Terrains", description = "Endpoints de gestion des terrains de padel. Chaque terrain appartient à un site et peut accueillir des matchs. Un site peut avoir plusieurs terrains avec des disponibilités différentes.")
public class TerrainController {

    private final TerrainService terrainService;
    private final TerrainMapper terrainMapper;

    @Operation(
            summary = "Créer un nouveau terrain",
            description = "Crée un nouveau terrain de padel et le lie à un site existant. Le site doit exister avant la création du terrain. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Terrain créé avec succès",
                    content = @Content(schema = @Schema(implementation = TerrainResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide ou erreur de validation",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Site introuvable",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TerrainResponse> create(@Valid @RequestBody TerrainRequest request) {
        Terrain terrain = terrainMapper.toEntity(request);
        Terrain saved = terrainService.create(terrain, request.getSiteId());
        return ResponseEntity.status(HttpStatus.CREATED).body(terrainMapper.toResponse(saved));
    }

    @Operation(
            summary = "Obtenir tous les terrains",
            description = "Retourne la liste de tous les terrains de padel sur l'ensemble des sites. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des terrains retournée avec succès",
                    content = @Content(schema = @Schema(implementation = TerrainResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<TerrainResponse>> getAll() {
        List<TerrainResponse> terrains = terrainService.getAll()
                .stream()
                .map(terrainMapper::toResponse)
                .toList();
        return ResponseEntity.ok(terrains);
    }

    @Operation(
            summary = "Obtenir un terrain par ID",
            description = "Retourne un terrain unique par son ID, y compris le site auquel il appartient. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terrain trouvé et retourné",
                    content = @Content(schema = @Schema(implementation = TerrainResponse.class))),
            @ApiResponse(responseCode = "404", description = "Terrain introuvable",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TerrainResponse> getById(
            @Parameter(description = "ID du terrain à récupérer", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(terrainMapper.toResponse(terrainService.getById(id)));
    }

    @Operation(
            summary = "Obtenir tous les terrains d'un site",
            description = "Retourne tous les terrains appartenant à un site spécifique. Utile pour afficher les terrains disponibles lorsqu'un membre souhaite réserver un match sur un site donné. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des terrains du site retournée avec succès",
                    content = @Content(schema = @Schema(implementation = TerrainResponse.class))),
            @ApiResponse(responseCode = "404", description = "Site introuvable",
                    content = @Content)
    })
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<TerrainResponse>> getBySiteId(
            @Parameter(description = "ID du site pour lequel récupérer les terrains", required = true)
            @PathVariable Long siteId) {
        List<TerrainResponse> terrains = terrainService.getBySiteId(siteId)
                .stream()
                .map(terrainMapper::toResponse)
                .toList();
        return ResponseEntity.ok(terrains);
    }

    @Operation(
            summary = "Mettre à jour un terrain",
            description = "Met à jour le nom d'un terrain existant. Le site ne peut pas être modifié après la création. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terrain mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = TerrainResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide ou erreur de validation",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Terrain introuvable",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TerrainResponse> update(
            @Parameter(description = "ID du terrain à mettre à jour", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TerrainRequest request) {
        Terrain terrain = terrainMapper.toEntity(request);
        Terrain updated = terrainService.update(id, terrain);
        return ResponseEntity.ok(terrainMapper.toResponse(updated));
    }

    @Operation(
            summary = "Supprimer un terrain",
            description = "Supprime définitivement un terrain et tous ses matchs associés. Cette action est irréversible. Nécessite le rôle ADMIN.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Terrain supprimé avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Terrain introuvable",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID du terrain à supprimer", required = true)
            @PathVariable Long id) {
        terrainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
