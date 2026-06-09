package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.get.OrderConfigGetDTO;
import com.example.projekt_mas.domain.order.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/config")
public class AppConfigController {

    @GetMapping("/orders")
    public OrderConfigGetDTO getOrderConfig() {
        System.out.println(LocalDateTime.now() + " - Fetching order configuration");
        return new OrderConfigGetDTO(
                Order.MINIMAL_ORDER_VALUE,
                Order.TIME_TO_PAY
        );
    }
}
