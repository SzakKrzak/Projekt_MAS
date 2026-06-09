package com.example.projekt_mas.domain.order;

import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.product.Furniture;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "customer_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private LocalDate createdOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatusType status;

    private LocalDate paymentDeadline;
    private LocalDate deliveryDeadline;
    private LocalDate completionDate;

    @Column(length = 1000)
    private String cancellationReason;

    @Column(length = 1000)
    private String feedback;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderLine> lines = new ArrayList<>();

    public static final BigDecimal MINIMAL_ORDER_VALUE = BigDecimal.valueOf(50);
    public static final Long TIME_TO_PAY = 3L;

    public Order(Client client, String deliveryAddress, LocalDate createdOn) {
        this(client, deliveryAddress, createdOn, createdOn == null ? null : createdOn.plusDays(TIME_TO_PAY));
    }

    public Order(Client client, String deliveryAddress, LocalDate createdOn, LocalDate paymentDeadline) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Delivery address cannot be blank");
        }
        if (createdOn == null) {
            throw new IllegalArgumentException("Created date cannot be null");
        }
        if (paymentDeadline == null) {
            throw new IllegalArgumentException("Payment deadline cannot be null");
        }
        if (paymentDeadline.isBefore(createdOn)) {
            throw new IllegalArgumentException("Payment deadline cannot be before created date");
        }
        this.client = client;
        this.deliveryAddress = deliveryAddress;
        this.createdOn = createdOn;
        this.status = OrderStatusType.CREATED;
        this.paymentDeadline = paymentDeadline;
        client.registerOrder(this);
    }

    public OrderLine addLine(Furniture furniture, int quantity) {
        if (status != OrderStatusType.CREATED) {
            throw new IllegalStateException("Only created orders can be modified");
        }
        if (quantity <= 0){
            throw new IllegalStateException("Quantity must be positive");
        }


        for (OrderLine line : lines){
            if (line.getFurniture().equals(furniture)){
                line.setQuantity(line.getQuantity() + quantity);
                return line;
            }
        }

        OrderLine line = new OrderLine(this, furniture, quantity);
        lines.add(line);
        return line;
    }

    public void removeLine(Furniture furniture){
        if (status != OrderStatusType.CREATED) {
            throw new IllegalStateException("Only created orders can be modified");
        }

        lines.removeIf(line -> line.getFurniture().equals(furniture));
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public BigDecimal calculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            sum = sum.add(line.getFurniture().getPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        return sum;
    }

    public BigDecimal calculateTotalAfterDiscount() {
        BigDecimal discount = BigDecimal.valueOf(client.getLoyaltyLevel())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return calculateTotal().multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPaidAmount() {
        return paidAmount != null ? paidAmount : BigDecimal.ZERO;
    }

    public void pay(LocalDate deliveryDeadline) {
        if (status != OrderStatusType.CREATED) {
            throw new IllegalStateException("Only created orders can be paid");
        }
        BigDecimal total = calculateTotalAfterDiscount();
        if (total.compareTo(MINIMAL_ORDER_VALUE) < 0) {
            throw new IllegalStateException("Order total must be at least " + MINIMAL_ORDER_VALUE);
        }
        if (deliveryDeadline == null) {
            throw new IllegalArgumentException("Delivery deadline cannot be null");
        }
        if (deliveryDeadline.isBefore(createdOn)) {
            throw new IllegalArgumentException("Delivery deadline cannot be before created date");
        }
        this.paidAmount = total;
        this.status = OrderStatusType.PAID;
        this.deliveryDeadline = deliveryDeadline;
    }

    public void complete(LocalDate completedOn) {
        if (status != OrderStatusType.PAID) {
            throw new IllegalStateException("Only paid orders can be completed");
        }
        if (completedOn == null) {
            throw new IllegalArgumentException("Completion date cannot be null");
        }
        if (completedOn.isBefore(createdOn)) {
            throw new IllegalArgumentException("Completion date cannot be before created date");
        }
        if (deliveryDeadline != null && completedOn.isAfter(deliveryDeadline)) {
            throw new IllegalArgumentException("Completion date cannot be after delivery deadline");
        }
        this.status = OrderStatusType.COMPLETED;
        this.completionDate = completedOn;
    }

    public void recordFeedback(Client client, String feedback) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (!this.client.equals(client)) {
            throw new IllegalStateException("Only the order owner can submit feedback");
        }
        if (status != OrderStatusType.COMPLETED) {
            throw new IllegalStateException("Only completed orders can receive feedback");
        }
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("Feedback cannot be blank");
        }
        this.feedback = feedback;
    }

    public void cancel(String reason) {
        if (status == OrderStatusType.COMPLETED || status == OrderStatusType.CANCELLED) {
            throw new IllegalStateException("Completed or cancelled orders cannot be cancelled");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason cannot be blank");
        }
        this.status = OrderStatusType.CANCELLED;
        this.cancellationReason = reason;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Order other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }
}
