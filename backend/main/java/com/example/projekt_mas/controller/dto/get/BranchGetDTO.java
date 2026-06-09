package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.branch.Branch;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record BranchGetDTO(
        Long id,
        String address,
        LocalTime openingTime,
        LocalTime closingTime,
        List<EmployeeShortGetDTO> employees
) {

    public static BranchGetDTO from(Branch branch) {
        List<EmployeeShortGetDTO> employees = new ArrayList<>();

        for (var employee : branch.getEmployees()) {
            employees.add(EmployeeShortGetDTO.from(employee));
        }

        return new BranchGetDTO(
                branch.getId(),
                branch.getAddress(),
                branch.getOpeningTime(),
                branch.getClosingTime(),
                employees
        );
    }
}
