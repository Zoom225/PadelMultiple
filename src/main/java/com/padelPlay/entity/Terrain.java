package com.padelPlay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "terrains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terrain extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private Double prix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @OneToMany(mappedBy = "terrain", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> matches;

    public Double getPrix() {
        return prix;
    }
}
