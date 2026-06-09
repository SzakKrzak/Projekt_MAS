package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;

import java.util.LinkedHashSet;
import java.util.Set;

public record EmployeeShortGetDTO(
        Long id,
        String firstName,
        String lastName,
        Set<String> roles
) {

    public static EmployeeShortGetDTO from(Employee employee) {
        Set<String> roles = new LinkedHashSet<>();

        for (EmployeeRole role : employee.getRoles()) {
            roles.add(role.name());
        }

        return new EmployeeShortGetDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                roles
        );
    }
}
