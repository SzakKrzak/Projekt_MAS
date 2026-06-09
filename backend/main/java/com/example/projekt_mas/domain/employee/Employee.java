package com.example.projekt_mas.domain.employee;

import com.example.projekt_mas.domain.branch.Branch;
import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.domain.product.Furniture;
import com.example.projekt_mas.domain.product.FurnitureCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "employee_roles", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "role", nullable = false)
    private Set<EmployeeRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("signedOn ASC")
    private final List<Contract> contracts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "designer", fetch = FetchType.LAZY)
    private final List<Furniture> designedFurniture = new ArrayList<>();

    private int fulfilledOrdersCount;
    private int servedCustomersCount;

    public Employee(String firstName, String lastName, Set<EmployeeRole> roles) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Employee must have at least one role (overlapping, complete)");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = EnumSet.copyOf(roles);
    }

    public Set<EmployeeRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public List<Contract> getContracts() {
        return Collections.unmodifiableList(contracts);
    }

    public Optional<Contract> getActiveContract() {
        return contracts.stream()
                .filter(Contract::isActive)
                .findFirst();
    }

    public boolean hasRole(EmployeeRole role) {
        return roles.contains(role);
    }

    public Contract addContract(LocalDate signedOn, BigDecimal salary) {
        Optional<Contract> activeContract = getActiveContract();
        if (activeContract.isPresent()) {
            Contract contract = activeContract.get();
            if (!signedOn.isAfter(contract.getSignedOn())) {
                throw new IllegalArgumentException("New contract must be signed after the active contract");
            }
            contract.endOn(signedOn.minusDays(1));
        }

        Contract contract = new Contract(this, signedOn, salary);
        contracts.add(contract);
        return contract;
    }

    public void assignBranch(Branch newBranch) {
        if (newBranch == null) {
            throw new IllegalArgumentException("Branch cannot be null");
        }
        if (!newBranch.equals(branch)) {
            newBranch.ensureCanAccept(this);
            Branch previousBranch = branch;
            branch = newBranch;

            if (previousBranch != null) {
                previousBranch.removeEmployee(this);
            }

            newBranch.assignEmployee(this);
        }
    }

    public List<Furniture> getDesignedFurniture() {
        return Collections.unmodifiableList(designedFurniture);
    }

    public void registerDesignedFurniture(Furniture furniture) {
        if (furniture == null) {
            throw new IllegalArgumentException("Furniture cannot be null");
        }
        if (!hasRole(EmployeeRole.DESIGNER)) {
            throw new IllegalStateException("Only designers can have designed furniture");
        }
        if (!equals(furniture.getDesigner())) {
            throw new IllegalArgumentException("Designed furniture must belong to this employee");
        }
        if (!designedFurniture.contains(furniture)) {
            designedFurniture.add(furniture);
        }
    }

    public void fulfillOrder(Order order) {
        if (!hasRole(EmployeeRole.WAREHOUSE_WORKER)) {
            throw new IllegalStateException("Only warehouse workers can fulfill orders");
        }
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        fulfilledOrdersCount++;
    }

    public int getFulfilledOrdersCount() {
        if (!hasRole(EmployeeRole.WAREHOUSE_WORKER)) {
            throw new IllegalStateException("Employee is not a warehouse worker");
        }
        return fulfilledOrdersCount;
    }

    public void serveCustomer(Client client) {
        if (!hasRole(EmployeeRole.CASHIER)) {
            throw new IllegalStateException("Only cashiers can serve customers");
        }
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        servedCustomersCount++;
    }

    public int getServedCustomersCount() {
        if (!hasRole(EmployeeRole.CASHIER)) {
            throw new IllegalStateException("Employee is not a cashier");
        }
        return servedCustomersCount;
    }

    public Furniture designFurniture(String name, BigDecimal price, List<String> materials, FurnitureCategory category) {
        if (!hasRole(EmployeeRole.DESIGNER)) {
            throw new IllegalStateException("Only designers can create furniture");
        }
        return Furniture.createDesignedBy(name, price, materials, category, this);
    }

    public void leaveBranch() {
        if (branch != null) {
            Branch previousBranch = branch;
            branch = null;
            previousBranch.removeEmployee(this);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Employee other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Employee.class.hashCode();
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " " + roles;
    }
}
