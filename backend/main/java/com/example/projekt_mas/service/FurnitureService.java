package com.example.projekt_mas.service;

import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.product.Furniture;
import com.example.projekt_mas.domain.product.FurnitureCategory;
import com.example.projekt_mas.repository.EmployeeRepository;
import com.example.projekt_mas.repository.FurnitureCategoryRepository;
import com.example.projekt_mas.repository.FurnitureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureCategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Furniture create(String name, BigDecimal price, List<String> materials, Long categoryId, Long designerId) {
        FurnitureCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        Employee designer = employeeRepository.findById(designerId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + designerId));
        return furnitureRepository.save(designer.designFurniture(name, price, materials, category));
    }

    @Transactional
    public FurnitureCategory createCategory(String name, String room) {
        return categoryRepository.save(new FurnitureCategory(name, room));
    }

    @Transactional(readOnly = true)
    public List<Furniture> getAll() {
        return furnitureRepository.findAll();
    }
}
