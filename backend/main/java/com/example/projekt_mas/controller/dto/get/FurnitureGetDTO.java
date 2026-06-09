package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.product.Furniture;

import java.math.BigDecimal;
import java.util.List;

public record FurnitureGetDTO(
        Long id,
        String name,
        BigDecimal price,
        List<String> materials,
        Long categoryId,
        String categoryName,
        Long designerId
) {

    public static FurnitureGetDTO from(Furniture furniture) {
        return new FurnitureGetDTO(
                furniture.getId(),
                furniture.getName(),
                furniture.getPrice(),
                furniture.getMaterials().stream().toList(),
                furniture.getCategory().getId(),
                furniture.getCategory().getName(),
                furniture.getDesigner().getId()
        );
    }
}
