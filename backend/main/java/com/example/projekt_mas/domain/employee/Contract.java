package com.example.projekt_mas.domain.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Column(nullable = false)
    private LocalDate signedOn;

    private LocalDate endedOn;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    Contract(Employee employee, LocalDate signedOn, BigDecimal salary) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (signedOn == null) {
            throw new IllegalArgumentException("Signed date cannot be null");
        }
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary must be non-negative");
        }
        this.employee = employee;
        this.signedOn = signedOn;
        this.salary = salary;
    }

    public boolean isActive() {
        return endedOn == null;
    }

    void endOn(LocalDate endDate) {
        if (endDate != null && endDate.isBefore(signedOn)) {
            throw new IllegalArgumentException("End date cannot be before signed date");
        }
        this.endedOn = endDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Contract other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Contract.class.hashCode();
    }
}
