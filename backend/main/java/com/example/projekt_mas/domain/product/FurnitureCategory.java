package com.example.projekt_mas.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "furniture_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FurnitureCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String room;

    @OneToMany(mappedBy = "category")
    private final List<Furniture> furnitureItems = new ArrayList<>();

    public FurnitureCategory(String name, String room) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (room == null || room.isBlank()) {
            throw new IllegalArgumentException("Room cannot be blank");
        }
        this.name = name;
        this.room = room;
    }

    public List<Furniture> getFurnitureItems() {
        return Collections.unmodifiableList(furnitureItems);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof FurnitureCategory other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return FurnitureCategory.class.hashCode();
    }
}
