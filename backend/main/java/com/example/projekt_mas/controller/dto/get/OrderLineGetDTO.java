package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.order.OrderLine;

import java.math.BigDecimal;

public record OrderLineGetDTO(
        Long id,
        Long furnitureId,
        String furnitureName,
        int quantity,
        BigDecimal unitPrice
) {

    public static OrderLineGetDTO from(OrderLine line) {
        return new OrderLineGetDTO(
                line.getId(),
                line.getFurniture().getId(),
                line.getFurniture().getName(),
                line.getQuantity(),
                line.getFurniture().getPrice()
        );
    }
}
