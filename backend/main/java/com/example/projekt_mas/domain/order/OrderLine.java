package com.example.projekt_mas.domain.order;

import com.example.projekt_mas.domain.product.Furniture;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    @Column(nullable = false)
    private int quantity;

    OrderLine(Order order, Furniture furniture, int quantity) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (furniture == null) {
            throw new IllegalArgumentException("Furniture cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.order = order;
        this.furniture = furniture;
        this.quantity = quantity;
    }

    public OrderLine setQuantity(int quantity){
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        return this;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof OrderLine other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return OrderLine.class.hashCode();
    }
}
