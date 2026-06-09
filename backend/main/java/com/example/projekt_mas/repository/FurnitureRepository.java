package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.product.Furniture;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FurnitureRepository extends JpaRepository<Furniture, Long> {

    @Override
    @EntityGraph(attributePaths = {"materials", "category", "designer"})
    List<Furniture> findAll();

    @Override
    @EntityGraph(attributePaths = {"materials", "category", "designer"})
    Optional<Furniture> findById(Long id);

    long countByDesignerId(Long designerId);
}
