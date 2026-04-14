package com.padel.padel_backend.mapper;

import com.padel.padel_backend.dto.request.MembreRequest;
import com.padel.padel_backend.dto.response.MembreResponse;
import com.padel.padel_backend.entity.Membre;
import org.springframework.stereotype.Component;

@Component
public class MembreMapper {

    public Membre toEntity(MembreRequest request) {
        return Membre.builder()
                .matricule(request.getMatricule())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .typeMembre(request.getTypeMembre())
                .build();
        // le site est résolu dans le service via siteId
    }

    public MembreResponse toResponse(Membre membre) {
        return MembreResponse.builder()
                .id(membre.getId())
                .matricule(membre.getMatricule())
                .nom(membre.getNom())
                .prenom(membre.getPrenom())
                .email(membre.getEmail())
                .typeMembre(membre.getTypeMembre())
                .siteId(membre.getSite() != null ? membre.getSite().getId() : null)
                .siteNom(membre.getSite() != null ? membre.getSite().getNom() : null)
                .solde(membre.getSolde())
                .build();
    }
}