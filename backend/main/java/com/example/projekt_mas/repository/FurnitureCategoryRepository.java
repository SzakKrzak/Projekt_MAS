package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.product.FurnitureCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FurnitureCategoryRepository extends JpaRepository<FurnitureCategory, Long> {
}
