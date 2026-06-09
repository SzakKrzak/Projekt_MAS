package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.employee.Contract;
import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record EmployeeGetDTO(
        Long id,
        String firstName,
        String lastName,
        Set<String> roles,
        Long branchId,
        String branchAddress,
        List<ContractGetDTO> contracts,
        Integer fulfilledOrdersCount,
        Integer servedCustomersCount
) {

    public static EmployeeGetDTO from(Employee employee) {
        Set<String> roles = new LinkedHashSet<>();
        for (EmployeeRole role : employee.getRoles()) {
            roles.add(role.name());
        }

        List<ContractGetDTO> contracts = new ArrayList<>();
        for (Contract contract : employee.getContracts()) {
            contracts.add(ContractGetDTO.from(contract));
        }

        Long branchId = null;
        String branchAddress = null;
        if (employee.getBranch() != null) {
            branchId = employee.getBranch().getId();
            branchAddress = employee.getBranch().getAddress();
        }

        Integer fulfilledOrdersCount = employee.hasRole(EmployeeRole.WAREHOUSE_WORKER)
                ? employee.getFulfilledOrdersCount()
                : null;
        Integer servedCustomersCount = employee.hasRole(EmployeeRole.CASHIER)
                ? employee.getServedCustomersCount()
                : null;

        return new EmployeeGetDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                roles,
                branchId,
                branchAddress,
                contracts,
                fulfilledOrdersCount,
                servedCustomersCount
        );
    }
}
