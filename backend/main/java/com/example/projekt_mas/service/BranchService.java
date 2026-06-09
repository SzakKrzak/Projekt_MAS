package com.example.projekt_mas.service;

import com.example.projekt_mas.domain.branch.Branch;
import com.example.projekt_mas.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional
    public Branch create(String address, LocalTime openingTime, LocalTime closingTime) {
        return branchRepository.save(new Branch(address, openingTime, closingTime));
    }

    @Transactional(readOnly = true)
    public List<Branch> getAll(){
        return branchRepository.findAll();
    }

}
