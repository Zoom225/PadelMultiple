package com.padel.padel_backend.service.impl;

import com.padel.padel_backend.entity.Match;
import com.padel.padel_backend.entity.Membre;
import com.padel.padel_backend.entity.Paiement;
import com.padel.padel_backend.entity.Reservation;
import com.padel.padel_backend.entity.enums.StatutPaiement;
import com.padel.padel_backend.exception.BusinessException;
import com.padel.padel_backend.exception.ResourceNotFoundException;
import com.padel.padel_backend.repository.MembreRepository;
import com.padel.padel_backend.repository.PaiementRepository;
import com.padel.padel_backend.service.PaiementService;
import com.padel.padel_backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ReservationService reservationService;
    private final MembreRepository membreRepository;

    @Override
    @Transactional
    public Paiement pay(Long reservationId, Long membreId) {
        Reservation reservation = reservationService.getById(reservationId);
        Membre membre = reservation.getMembre();

        // règle : seul le membre concerné peut payer
        if (!membre.getId().equals(membreId)) {
            throw new BusinessException("Only the member of this reservation can pay");
        }

        Paiement paiement = reservation.getPaiement();
        if (paiement == null) {
            throw new ResourceNotFoundException("No payment found for reservation : " + reservationId);
        }

        if (paiement.getStatut() == StatutPaiement.PAYE) {
            throw new BusinessException("Payment already done for this reservation");
        }

        // règle : si solde dû, on l'ajoute au montant
        double montantFinal = paiement.getMontant();
        if (membre.getSolde() > 0.0) {
            montantFinal += membre.getSolde();
            log.info("Outstanding balance of {} added to payment for member {}",
                    membre.getSolde(), membreId);
            membre.setSolde(0.0);
            membreRepository.save(membre);
        }

        paiement.setMontant(montantFinal);
        paiement.setStatut(StatutPaiement.PAYE);
        paiement.setDatePaiement(LocalDateTime.now());
        paiementRepository.save(paiement);

        // confirmer la réservation après paiement
        reservationService.confirm(reservationId);

        log.info("Payment done for reservation {} by member {}", reservationId, membreId);

        return paiement;
    }

    @Override
    public Paiement getById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id : " + id));
    }

    @Override
    public Paiement getByReservationId(Long reservationId) {
        return paiementRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reservation : " + reservationId));
    }

    @Override
    public List<Paiement> getByMembreId(Long membreId) {
        return paiementRepository.findByReservationMembreId(membreId);
    }

    @Override
    @Transactional
    public void checkUnpaidBeforeMatch() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // récupérer toutes les réservations EN_ATTENTE pour les matchs de demain
        paiementRepository.findByStatut(StatutPaiement.EN_ATTENTE)
                .stream()
                .filter(p -> p.getReservation().getMatch()
                        .getDate().equals(tomorrow))
                .forEach(p -> {
                    Reservation reservation = p.getReservation();
                    Match match = reservation.getMatch();

                    // annuler la réservation non payée
                    reservationService.cancel(reservation.getId());

                    // ajouter le solde dû à l'organisateur si match public non complet
                    double partManquante = match.getPrixParJoueur();
                    Membre organisateur = match.getOrganisateur();
                    organisateur.setSolde(organisateur.getSolde() + partManquante);
                    membreRepository.save(organisateur);

                    log.info("Unpaid reservation {} cancelled, balance {} added to organizer {}",
                            reservation.getId(), partManquante, organisateur.getId());
                });
    }
}