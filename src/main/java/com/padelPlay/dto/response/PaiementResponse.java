package com.padel.padel_backend.dto.response;

import com.padel.padel_backend.entity.enums.StatutPaiement;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementResponse {
    private Long id;
    private Double montant;
    private StatutPaiement statut;
    private LocalDateTime datePaiement;
}