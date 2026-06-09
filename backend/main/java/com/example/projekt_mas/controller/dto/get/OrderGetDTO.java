package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.domain.order.OrderStatusType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record OrderGetDTO(
        Long id,
        Long clientId,
        String deliveryAddress,
        LocalDate createdOn,
        OrderStatusType status,
        LocalDate paymentDeadline,
        LocalDate deliveryDeadline,
        LocalDate completionDate,
        String cancellationReason,
        String feedback,
        List<OrderLineGetDTO> lines
) {

    public static OrderGetDTO from(Order order) {
        List<OrderLineGetDTO> lines = new ArrayList<>();

        for (var line : order.getLines()) {
            lines.add(OrderLineGetDTO.from(line));
        }

        return new OrderGetDTO(
                order.getId(),
                order.getClient().getId(),
                order.getDeliveryAddress(),
                order.getCreatedOn(),
                order.getStatus(),
                order.getPaymentDeadline(),
                order.getDeliveryDeadline(),
                order.getCompletionDate(),
                order.getCancellationReason(),
                order.getFeedback(),
                lines
        );
    }
}
