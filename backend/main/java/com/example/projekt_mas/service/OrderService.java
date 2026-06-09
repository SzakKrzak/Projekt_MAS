package com.example.projekt_mas.service;

import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.domain.order.OrderStatusType;
import com.example.projekt_mas.domain.product.Furniture;
import com.example.projekt_mas.repository.ClientRepository;
import com.example.projekt_mas.repository.FurnitureRepository;
import com.example.projekt_mas.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final FurnitureRepository furnitureRepository;

    @Transactional
    public Order create(Long clientId, String deliveryAddress) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
        Order order = new Order(client, deliveryAddress, LocalDate.now());
        return orderRepository.save(order);
    }

    @Transactional
    public Order addLine(Long orderId, Long furnitureId, int quantity) {
        Order order = getById(orderId);
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new IllegalArgumentException("Furniture not found: " + furnitureId));
        order.addLine(furniture, quantity);
        return orderRepository.save(order);
    }

    @Transactional
    public Order pay(Long orderId, LocalDate deliveryDeadline) {
        Order order = getById(orderId);
        order.pay(deliveryDeadline);
        return orderRepository.save(order);
    }

    @Transactional
    public Order complete(Long orderId, LocalDate completedOn) {
        Order order = getById(orderId);
        order.complete(completedOn);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancel(Long orderId, String reason) {
        Order order = getById(orderId);
        order.cancel(reason);
        return orderRepository.save(order);
    }

    @Transactional
    public int cancelOverdueCreatedOrders(LocalDate today, String reason) {
        if (today == null) {
            throw new IllegalArgumentException("Current date cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }

        List<Order> overdueCreatedOrders = new ArrayList<>();

        for (Order order : orderRepository.findAll()) {
            boolean isCreated = order.getStatus() == OrderStatusType.CREATED;
            boolean paymentDatePassed = order.getPaymentDeadline() != null
                    && order.getPaymentDeadline().isBefore(today);

            if (isCreated && paymentDatePassed) {
                overdueCreatedOrders.add(order);
            }
        }

        for (Order order : overdueCreatedOrders) {
            order.cancel(reason);
        }

        orderRepository.saveAll(overdueCreatedOrders);
        return overdueCreatedOrders.size();
    }

    @Transactional(readOnly = true)
    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalForOrder(Long orderId) {
        Order order = getById(orderId);
        return order.calculateTotalAfterDiscount();
    }
}
