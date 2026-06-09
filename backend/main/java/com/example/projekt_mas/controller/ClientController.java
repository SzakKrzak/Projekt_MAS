package com.example.projekt_mas.controller;

import com.example.projekt_mas.controller.dto.get.ClientGetDTO;
import com.example.projekt_mas.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientGetDTO> getAll() {
        System.out.println(LocalDateTime.now() + " - Fetching client list");
        return clientService.getAll().stream()
                .map(ClientGetDTO::from)
                .toList();
    }

}
