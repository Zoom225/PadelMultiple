package com.padelPlay.controller;

import com.padelPlay.dto.response.PaiementResponse;
import com.padelPlay.mapper.PaiementMapper;
import com.padelPlay.service.PaiementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Endpoints de gestion des paiements des matchs " +
        "Un paiement est automatiquement créé avec le statut EN_ATTENTE lorsqu'une réservation est effectuée." +
        "Cycle de vie du paiement : " +
        "EN_ATTENTE (créé lors de la réservation) → PAYÉ (paiement confirmé) → REMBOURSÉ (en cas d'annulation de la réservation)." +
        " Règles métier : " +
        "- Chaque match coûte 60 € divisés par 4 joueurs = 15 € par joueur." +
        "- Le paiement doit être effectué avant le jour du match." +
        "- Si un membre a un solde impayé, celui-ci est automatiquement ajouté à son prochain paiement." +
        "- Si un joueur n'a pas payé la veille du match, sa réservation est automatiquement annulée" +
        "et la place est libérée pour d'autres membres." +
        "- Si l'organisateur d'un match public finit par couvrir les joueurs manquants," +
        "les parts impayées sont ajoutées à son solde impayé.")
public class PaiementController {

    private final PaiementService paiementService;
    private final PaiementMapper paiementMapper;

    @Operation(
            summary = "Pay for a reservation",
            description = "Traite le paiement d'une réservation donnée. " +
                    "Seul le membre titulaire de la réservation peut effectuer le paiement." +
                    "Une fois le paiement traité : " +
                    "1. Si le membre a un solde impayé, celui-ci est automatiquement ajouté au montant du paiement." +
                    "2. Le solde impayé est effacé du compte du membre." +
                    "3. Le statut de paiement passe de EN_ATTENTE à PAYE." +
                    "4. Le statut de la réservation passe de EN_ATTENTE à CONFIRMEE. +" +
                    "5. Le nombre de joueurs participant au match (nbJoueursActuels) est incrémenté." +
                    "6. Si nbJoueursActuels atteint 4, le statut du match passe à COMPLET." +
                    "Pour les matchs PUBLICS, le paiement constitue l'étape de validation — premier payé = premier servi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successfully processed and reservation confirmed",
                    content = @Content(schema = @Schema(implementation = PaiementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payment already done, or member does not own this reservation",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation or payment not found",
                    content = @Content)
    })
    @PostMapping("/reservation/{reservationId}/membre/{membreId}")
    public ResponseEntity<PaiementResponse> pay(
            @Parameter(description = "ID of the reservation to pay for", required = true)
            @PathVariable Long reservationId,
            @Parameter(description = "ID of the member making the payment — must match the reservation owner", required = true)
            @PathVariable Long membreId) {
        return ResponseEntity.ok(
                paiementMapper.toResponse(paiementService.pay(reservationId, membreId))
        );
    }

    @Operation(
            summary = "Get a payment by ID",
            description = "Returns a single payment by its ID. " +
                    "Includes the payment amount, current status, and payment date if already processed. " +
                    "Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found and returned",
                    content = @Content(schema = @Schema(implementation = PaiementResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaiementResponse> getById(
            @Parameter(description = "ID of the payment to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(paiementMapper.toResponse(paiementService.getById(id)));
    }

    @Operation(
            summary = "Get the payment linked to a reservation",
            description = "Returns the payment associated with a specific reservation. " +
                    "Each reservation has exactly one payment. " +
                    "Useful to check the payment status before allowing a member to join a match. " +
                    "Publicly accessible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found and returned",
                    content = @Content(schema = @Schema(implementation = PaiementResponse.class))),
            @ApiResponse(responseCode = "404", description = "No payment found for this reservation",
                    content = @Content)
    })
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaiementResponse> getByReservationId(
            @Parameter(description = "ID of the reservation to retrieve the payment for", required = true)
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(
                paiementMapper.toResponse(paiementService.getByReservationId(reservationId))
        );
    }

    @Operation(
            summary = "Récupérer tous les paiements d'un membre",
            description = "Renvoie l'historique complet des paiements d'un membre pour l'ensemble de ses réservations." +
                    "Comprend les paiements de tous les statuts : EN_ATTENTE, PAYE et REMBOURSE. " +
                    "Utile pour l'interface membre afin d'afficher l'historique des paiements" +
                    "et pour l'interface d'administration afin de suivre les revenus par membre." +
                    "Accessible au public."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of payments for the member returned successfully",
                    content = @Content(schema = @Schema(implementation = PaiementResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content)
    })
    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<PaiementResponse>> getByMembreId(
            @Parameter(description = "ID of the member to retrieve payments for", required = true)
            @PathVariable Long membreId) {
        List<PaiementResponse> paiements = paiementService.getByMembreId(membreId)
                .stream()
                .map(paiementMapper::toResponse)
                .toList();
        return ResponseEntity.ok(paiements);
    }
}
