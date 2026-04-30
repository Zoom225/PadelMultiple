package com.padelPlay.entity;

import com.padelPlay.entity.enums.StatutMatch;
import com.padelPlay.entity.enums.TypeMatch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terrain_id", nullable = false)
    private Terrain terrain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id", nullable = false)
    private Membre organisateur;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMatch typeMatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMatch statut;

    @Column(nullable = false)
    private Integer nbJoueursActuels;

    @Column(nullable = false)
    private Double prixTotal;

    @Column(nullable = false)
    private Double prixParJoueur;

    private LocalDateTime dateConversionPublic;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    public java.time.LocalDate getDate() {
        return dateDebut != null ? dateDebut.toLocalDate() : null;
    }

    public java.time.LocalTime getHeureDebut() {
        return dateDebut != null ? dateDebut.toLocalTime() : null;
    }

    public java.time.LocalTime getHeureFin() {
        return dateFin != null ? dateFin.toLocalTime() : null;
    }

    public void setHeureFin(java.time.LocalTime heureFin) {
        if (dateFin != null && heureFin != null) {
            this.dateFin = java.time.LocalDateTime.of(dateFin.toLocalDate(), heureFin);
        } else if (dateDebut != null && heureFin != null) {
            this.dateFin = java.time.LocalDateTime.of(dateDebut.toLocalDate(), heureFin);
        }
    }
}
