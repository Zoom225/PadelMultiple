package com.padelPlay.repository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.padelPlay.entity.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerrainRepository extends JpaRepository<Terrain, Long> {
    @EntityGraph(attributePaths = "site")
    List<Terrain> findBySiteId(Long siteId);
}
