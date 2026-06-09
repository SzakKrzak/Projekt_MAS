package com.example.projekt_mas.domain.client;

import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.domain.order.OrderStatusType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "clients")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "client_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Client {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String PHONE_NUMBER_PATTERN = "^\\+?[0-9][0-9\\s-]{6,18}[0-9]$";
    private static final int MAX_LOYALTY_LEVEL = 20;
    private static final BigDecimal LOYALTY_LEVEL_SPEND_UNIT = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Order> orders = new ArrayList<>();

    protected Client(String address, String phoneNumber, String email) {
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    public void setAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be blank");
        }
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be blank");
        }
        if (!phoneNumber.matches(PHONE_NUMBER_PATTERN)) {
            throw new IllegalArgumentException("Phone number has invalid format");
        }
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Email has invalid format");
        }
        this.email = email;
    }

    public void registerOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!equals(order.getClient())) {
            throw new IllegalStateException("Only the order owner can register an order");
        }
        if (!orders.contains(order)) {
            orders.add(order);
        }
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void submitFeedback(Order order, String feedback) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!equals(order.getClient())) {
            throw new IllegalStateException("Only the order owner can submit feedback");
        }
        order.recordFeedback(this, feedback);
    }

    public int getLoyaltyLevel() {
        BigDecimal spent = getCompletedOrdersSpentAmount();
        int level = 0;

        for (int currLevel = 1; currLevel <= MAX_LOYALTY_LEVEL; currLevel++) {
            BigDecimal requiredSpend = LOYALTY_LEVEL_SPEND_UNIT
                    .multiply(BigDecimal.valueOf((long) currLevel * currLevel));

            if (spent.compareTo(requiredSpend) < 0) {
                break;
            }
            level = currLevel;
        }

        return level;
    }

    public BigDecimal getCompletedOrdersSpentAmount() {
        BigDecimal sum = BigDecimal.ZERO;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatusType.COMPLETED || order.getStatus() == OrderStatusType.PAID) {
                sum = sum.add(order.getPaidAmount());
            }
        }

        return sum;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Client other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Client.class.hashCode();
    }
}
