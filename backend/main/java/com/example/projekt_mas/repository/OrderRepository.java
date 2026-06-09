package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.order.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"client", "lines", "lines.furniture"})
    Optional<Order> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"client", "lines", "lines.furniture"})
    List<Order> findAll();
}
