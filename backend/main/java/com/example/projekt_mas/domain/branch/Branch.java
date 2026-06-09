package com.example.projekt_mas.domain.branch;

import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "branches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalTime openingTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @OneToMany(mappedBy = "branch")
    private final List<Employee> employees = new ArrayList<>();

    public Branch(String address, LocalTime openingTime, LocalTime closingTime) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be blank");
        }
        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException("Opening and closing time cannot be null");
        }
        if (!closingTime.isAfter(openingTime)) {
            throw new IllegalArgumentException("Closing time must be after opening time");
        }
        this.address = address;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public void assignEmployee(Employee employee) {
        if (employee == null){
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if(!this.employees.contains(employee)) {
            ensureCanAccept(employee);
            this.employees.add(employee);
        }
        if (!equals(employee.getBranch())) {
            employee.assignBranch(this);
        }
    }

    public void removeEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employees.remove(employee) && equals(employee.getBranch())) {
            employee.leaveBranch();
        }
    }

    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    public void ensureCanAccept(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        Optional<Employee> manager = getManager();
        if (employee.hasRole(EmployeeRole.MANAGER) && manager.isPresent() && !manager.get().equals(employee)) {
            throw new IllegalArgumentException("Manager is already assigned");
        }
    }

    public Optional<Employee> getManager() {
        return employees.stream()
                .filter(e -> e.hasRole(EmployeeRole.MANAGER))
                .findFirst();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Branch other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Branch.class.hashCode();
    }
}
