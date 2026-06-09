package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.branch.Branch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Override
    @EntityGraph(attributePaths = {"employees", "employees.roles"})
    List<Branch> findAll();

    @Override
    @EntityGraph(attributePaths = {"employees", "employees.roles"})
    Optional<Branch> findById(Long id);
}
