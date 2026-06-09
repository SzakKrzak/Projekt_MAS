package com.example.projekt_mas.controller.dto.create;

import java.time.LocalDate;

public record OrderCreateDTO(Long clientId, String deliveryAddress, LocalDate paymentDeadline) {
}
