package com.example.projekt_mas.service;

import com.example.projekt_mas.domain.branch.Branch;
import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;
import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.repository.BranchRepository;
import com.example.projekt_mas.repository.ClientRepository;
import com.example.projekt_mas.repository.EmployeeRepository;
import com.example.projekt_mas.repository.FurnitureRepository;
import com.example.projekt_mas.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final FurnitureRepository furnitureRepository;
    private final BranchRepository branchRepository;
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Employee create(String firstName, String lastName, Set<EmployeeRole> roles) {
        Employee employee = new Employee(firstName, lastName, roles);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee addContract(Long employeeId, LocalDate signedOn, BigDecimal salary) {
        Employee employee = getById(employeeId);
        employee.addContract(signedOn, salary);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee assignBranch(Long employeeId, Long branchId) {
        Employee employee = getById(employeeId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));
        employee.assignBranch(branch);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee recordWarehouseFulfillment(Long employeeId, Long orderId) {
        Employee employee = getById(employeeId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        employee.fulfillOrder(order);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee recordCustomerServed(Long employeeId, Long clientId) {
        Employee employee = getById(employeeId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
        employee.serveCustomer(client);
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Employee getById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
    }

    @Transactional(readOnly = true)
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBonus(Long employeeId) {
        Employee employee = getById(employeeId);
        BigDecimal total = BigDecimal.ZERO;

        if (employee.hasRole(EmployeeRole.MANAGER)) {
            total = total.add(BigDecimal.valueOf(500));
        }
        if (employee.hasRole(EmployeeRole.WAREHOUSE_WORKER)) {
            total = total.add(BigDecimal.valueOf(employee.getFulfilledOrdersCount() * 15L));
        }
        if (employee.hasRole(EmployeeRole.CASHIER)) {
            total = total.add(BigDecimal.valueOf(employee.getServedCustomersCount() * 5L));
        }
        if (employee.hasRole(EmployeeRole.DESIGNER)) {
            long designedCount = furnitureRepository.countByDesignerId(employee.getId());
            total = total.add(BigDecimal.valueOf(designedCount * 100L));
        }
        return total;
    }
}
