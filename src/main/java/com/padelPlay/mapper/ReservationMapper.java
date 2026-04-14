package com.padel.padel_backend.mapper;

import com.padel.padel_backend.dto.response.ReservationResponse;
import com.padel.padel_backend.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationMapper {

    private final PaiementMapper paiementMapper;

    public ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .matchId(reservation.getMatch().getId())
                .matchDateTime(LocalDateTime.of(
                        reservation.getMatch().getDate(),
                        reservation.getMatch().getHeureDebut()))
                .membreId(reservation.getMembre().getId())
                .membreNom(reservation.getMembre().getNom()
                        + " " + reservation.getMembre().getPrenom())
                .statut(reservation.getStatut())
                .paiement(reservation.getPaiement() != null
                        ? paiementMapper.toResponse(reservation.getPaiement())
                        : null)
                .build();
    }
}