package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.get.FurnitureGetDTO;
import com.example.projekt_mas.service.FurnitureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/furniture")
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    @GetMapping
    public List<FurnitureGetDTO> getAll() {
        System.out.println(LocalDateTime.now() + " - Fetching furniture list");
        return furnitureService.getAll().stream()
                .map(FurnitureGetDTO::from)
                .toList();
    }

}
