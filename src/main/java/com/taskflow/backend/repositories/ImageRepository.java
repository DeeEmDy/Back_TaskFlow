package com.taskflow.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskflow.backend.entities.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    // Puedes eliminar este método, ya que JpaRepository ya proporciona uno con la misma firma
}
