package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.employee.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractGetDTO(
        Long id,
        LocalDate signedOn,
        LocalDate endedOn,
        BigDecimal salary
) {

    public static ContractGetDTO from(Contract contract) {
        return new ContractGetDTO(
                contract.getId(),
                contract.getSignedOn(),
                contract.getEndedOn(),
                contract.getSalary()
        );
    }
}
