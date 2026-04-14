package com.padel.padel_backend.service;

import com.padel.padel_backend.entity.Site;

import java.util.List;

public interface SiteService {
    Site create(Site site);
    Site getById(Long id);
    List<Site> getAll();
    Site update(Long id, Site site);
    void delete(Long id);
}
