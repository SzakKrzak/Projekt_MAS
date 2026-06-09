package com.example.projekt_mas.repository;

import com.example.projekt_mas.domain.employee.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = {"contracts", "branch", "roles"})
    List<Employee> findAll();

    @Override
    @EntityGraph(attributePaths = {"contracts", "branch","roles"})
    Optional<Employee> findById(Long id);

}
