package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.get.EmployeeGetDTO;
import com.example.projekt_mas.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeGetDTO> getAll() {
        System.out.println(LocalDateTime.now() + " - Fetching employee list");
        return employeeService.getAll().stream()
                .map(EmployeeGetDTO::from)
                .toList();
    }
}
