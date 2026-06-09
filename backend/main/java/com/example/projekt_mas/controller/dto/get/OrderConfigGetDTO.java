package com.example.projekt_mas.controller.dto.get;

import java.math.BigDecimal;

public record OrderConfigGetDTO(
        BigDecimal minimalOrderValue,
        Long timeToPay
) {
}
