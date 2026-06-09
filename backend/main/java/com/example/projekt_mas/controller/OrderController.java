package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.create.OrderCreateDTO;
import com.example.projekt_mas.controller.dto.create.OrderLineCreateDTO;
import com.example.projekt_mas.controller.dto.get.OrderGetDTO;
import com.example.projekt_mas.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @GetMapping
    public List<OrderGetDTO> getAll(){
        System.out.println(LocalDateTime.now() + " - Fetching order list");
        return orderService.getAll().stream()
                .map(OrderGetDTO::from)
                .toList();
    }
    @GetMapping("/{orderId}")
    public OrderGetDTO getById(@PathVariable Long orderId) {
        System.out.println(LocalDateTime.now() + " - Fetching order with id: " + orderId);
        return OrderGetDTO.from(orderService.getById(orderId));
    }

    @PostMapping
    public OrderGetDTO create(@RequestBody OrderCreateDTO request) {
        System.out.println(LocalDateTime.now() + " - Creating order for client with id: " + request.clientId());
        return OrderGetDTO.from(orderService.create(request.clientId(), request.deliveryAddress()));
    }

    @PostMapping("/{orderId}/lines")
    public OrderGetDTO addLine(@PathVariable Long orderId, @RequestBody OrderLineCreateDTO request) {
        System.out.println(LocalDateTime.now() + " - Adding line to order with id: " + orderId);
        return OrderGetDTO.from(orderService.addLine(orderId, request.furnitureId(), request.quantity()));
    }

    @PostMapping("/{orderId}/pay")
    public OrderGetDTO pay(@PathVariable Long orderId, @RequestBody PayOrderRequest request) {
        System.out.println(LocalDateTime.now() + " - Paying order with id: " + orderId);
        return OrderGetDTO.from(orderService.pay(orderId, request.deliveryDeadline()));
    }

    public record PayOrderRequest(LocalDate deliveryDeadline) {
    }
}
