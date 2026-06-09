package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.client.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Override
    @EntityGraph(attributePaths = "orders")
    List<Client> findAll();

    @Override
    @EntityGraph(attributePaths = "orders")
    Optional<Client> findById(Long id);
}
