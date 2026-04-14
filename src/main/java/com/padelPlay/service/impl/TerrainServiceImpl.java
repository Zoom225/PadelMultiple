package com.padel.padel_backend.service.impl;

import com.padel.padel_backend.entity.Site;
import com.padel.padel_backend.entity.Terrain;
import com.padel.padel_backend.exception.ResourceNotFoundException;
import com.padel.padel_backend.repository.TerrainRepository;
import com.padel.padel_backend.service.SiteService;
import com.padel.padel_backend.service.TerrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainServiceImpl implements TerrainService {

    private final TerrainRepository terrainRepository;
    private final SiteService siteService;

    @Override
    public Terrain create(Terrain terrain, Long siteId) {
        Site site = siteService.getById(siteId);
        terrain.setSite(site);
        log.info("Terrain created for site {}", siteId);
        return terrainRepository.save(terrain);
    }

    @Override
    public Terrain getById(Long id) {
        return terrainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terrain not found with id : " + id));
    }

    @Override
    public List<Terrain> getAll() {
        return terrainRepository.findAll();
    }

    @Override
    public List<Terrain> getBySiteId(Long siteId) {
        return terrainRepository.findBySiteId(siteId);
    }

    @Override
    public Terrain update(Long id, Terrain terrain) {
        Terrain existing = getById(id);
        existing.setNom(terrain.getNom());
        return terrainRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Terrain existing = getById(id);
        terrainRepository.delete(existing);
    }
}
