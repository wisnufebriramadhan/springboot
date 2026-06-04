package com.startechinnovation.userapi.service;

import com.startechinnovation.userapi.dto.BranchRequest;
import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public Branch createBranch(BranchRequest request) {
        if (branchRepository.findByBranchCode(request.getBranchCode()).isPresent()) {
            throw new RuntimeException("Branch code already exists");
        }
        
        Branch branch = Branch.builder()
                .name(request.getName())
                .branchCode(request.getBranchCode())
                .build();
        
        return branchRepository.save(branch);
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}
