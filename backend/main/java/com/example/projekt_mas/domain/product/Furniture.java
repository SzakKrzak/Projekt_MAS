package com.example.projekt_mas.domain.product;

import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "furniture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Furniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ElementCollection
    @CollectionTable(name = "furniture_materials", joinColumns = @JoinColumn(name = "furniture_id"))
    @Column(name = "material", nullable = false)
    private List<String> materials = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FurnitureCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designer_id", nullable = false)
    private Employee designer;

    public static Furniture createDesignedBy(
            String name,
            BigDecimal price,
            List<String> materials,
            FurnitureCategory category,
            Employee designer
    ) {
        return new Furniture(name, price, materials, category, designer);
    }

    private Furniture(String name, BigDecimal price, List<String> materials, FurnitureCategory category, Employee designer) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("Furniture must have at least one material [1..*]");
        }
        for (String material : materials) {
            if (material == null || material.isBlank()) {
                throw new IllegalArgumentException("Material cannot be blank");
            }
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.name = name;
        this.price = price;
        this.materials = new ArrayList<>(materials);
        this.category = category;
        setDesigner(designer);
    }

    private void setDesigner(Employee designer) {
        if (designer == null) {
            throw new IllegalArgumentException("Designer cannot be null");
        }
        if (!designer.hasRole(EmployeeRole.DESIGNER)) {
            throw new IllegalStateException("Only designers can be assigned as furniture designers");
        }
        this.designer = designer;
        designer.registerDesignedFurniture(this);
    }

    public List<String> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    public void assignCategory(FurnitureCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        this.category = category;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Furniture other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Furniture.class.hashCode();
    }
}
