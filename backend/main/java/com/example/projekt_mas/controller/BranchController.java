package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.get.BranchGetDTO;
import com.example.projekt_mas.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public List<BranchGetDTO> getAll() {
        System.out.println(LocalDateTime.now() + " - Fetching branch list");
        return branchService.getAll().stream()
                .map(BranchGetDTO::from)
                .toList();
    }
}
