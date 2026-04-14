package com.padel.padel_backend.dto.response;

import com.padel.padel_backend.entity.enums.StatutReservation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private Long matchId;
    private LocalDateTime matchDateTime;
    private Long membreId;
    private String membreNom;
    private StatutReservation statut;
    private PaiementResponse paiement;
}