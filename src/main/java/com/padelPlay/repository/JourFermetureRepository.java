package com.padelPlay.repository;

import com.padelPlay.entity.JourFermeture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourFermetureRepository extends JpaRepository<JourFermeture, Long> {

}
