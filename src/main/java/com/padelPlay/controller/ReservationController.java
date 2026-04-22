package com.padelPlay.controller;

import com.padelPlay.dto.request.ReservationRequest;
import com.padelPlay.dto.response.ReservationResponse;
import com.padelPlay.entity.Reservation;
import com.padelPlay.mapper.ReservationMapper;
import com.padelPlay.service.ReservationService;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Endpoints de gestion des réservations de matchs. " +
        "Une réservation lie un membre à un match et génère automatiquement un paiement en attente. " +
        "Cycle de vie d'une réservation : " +
        "EN_ATTENTE (créée, paiement en attente) → CONFIRMEE (paiement effectué) → ANNULEE (annulée). " +
        "Règles métier : " +
        "- Un membre ne peut pas réserver une place dans le même match deux fois. " +
        "- Un membre avec une pénalité active ne peut pas effectuer de réservation. " +
        "- Un membre ayant un solde impayé ne peut pas effectuer de réservation. " +
        "- Pour les matchs PRIVATE, seul l'organisateur peut ajouter des joueurs. " +
        "- Pour les matchs PUBLIC, tout membre éligible peut rejoindre en payant (premier arrivé, premier servi). " +
        "- Un membre SITE ne peut réserver que sur son propre site.")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @Operation(
            summary = "Créer une nouvelle réservation",
            description = "Crée une réservation pour un membre sur un match spécifique. " +
                    "Un paiement en attente (EN_ATTENTE) est automatiquement créé en même temps que la réservation. " +
                    "La réservation n'est pas confirmée tant que le paiement n'est pas effectué via POST /api/paiements. " +
                    "Règles métier appliquées : " +
                    "1. Le match ne doit pas être complet (nbJoueursActuels < 4) et ne doit pas être annulé. " +
                    "2. Le membre ne doit pas déjà être inscrit à ce match. " +
                    "3. Le membre ne doit pas avoir de pénalité active. " +
                    "4. Le membre ne doit pas avoir de solde impayé. " +
                    "5. Pour les matchs PRIVATE, seul l'organisateur peut ajouter des joueurs. " +
                    "6. Un membre SITE ne peut réserver que sur son propre site."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation créée avec succès avec un paiement en attente",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Violation d'une règle métier — match complet, déjà inscrit, pénalité, solde impayé, mauvais site ou restriction de match privé",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Match ou membre introuvable",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.create(
                request.getMatchId(),
                request.getMembreId(),
                request.getRequesterId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationMapper.toResponse(reservation));
    }

    @Operation(
            summary = "Obtenir une réservation par ID",
            description = "Retourne une réservation unique par son ID. " +
                    "Inclut les détails du match associé, les informations du membre, le statut de la réservation " +
                    "et le paiement lié avec son statut actuel. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation trouvée et retournée",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(
            @Parameter(description = "ID de la réservation à récupérer", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(
                reservationMapper.toResponse(reservationService.getById(id))
        );
    }

    @Operation(
            summary = "Obtenir toutes les réservations d'un match",
            description = "Retourne toutes les réservations liées à un match spécifique. " +
                    "Utile pour l'interface admin afin de voir qui a rejoint un match " +
                    "et le statut de paiement de chaque joueur. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réservations du match retournée avec succès",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Match introuvable",
                    content = @Content)
    })
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<ReservationResponse>> getByMatchId(
            @Parameter(description = "ID du match pour lequel récupérer les réservations", required = true)
            @PathVariable Long matchId) {
        List<ReservationResponse> reservations = reservationService.getByMatchId(matchId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(reservations);
    }

    @Operation(
            summary = "Obtenir toutes les réservations d'un membre",
            description = "Retourne toutes les réservations effectuées par un membre spécifique sur l'ensemble des matchs. " +
                    "Inclut les matchs passés et à venir avec leur statut de paiement. " +
                    "Utilisé par l'interface membre pour afficher son historique de réservations. Accessible publiquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réservations du membre retournée avec succès",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Membre introuvable",
                    content = @Content)
    })
    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<ReservationResponse>> getByMembreId(
            @Parameter(description = "ID du membre pour lequel récupérer les réservations", required = true)
            @PathVariable Long membreId) {
        List<ReservationResponse> reservations = reservationService.getByMembreId(membreId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(reservations);
    }

    @Operation(
            summary = "Annuler une réservation",
            description = "Annule une réservation existante et libère la place dans le match. " +
                    "Si le paiement était déjà effectué (PAYE), il est automatiquement marqué comme REMBOURSE. " +
                    "Le nombre de joueurs du match (nbJoueursActuels) est décrémenté automatiquement. " +
                    "Si le match était COMPLET, il repasse au statut PLANIFIE après annulation, " +
                    "ce qui le rend de nouveau rejoignable par d'autres membres.",
            security = @SecurityRequirement(name = "Bearer Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réservation annulée avec succès"),
            @ApiResponse(responseCode = "400", description = "La réservation est déjà annulée",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé — jeton admin requis",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable",
                    content = @Content)
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @Parameter(description = "ID de la réservation à annuler", required = true)
            @PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
