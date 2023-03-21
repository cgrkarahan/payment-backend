package com.paymentology.paymentreconciliation.service;

import com.paymentology.paymentreconciliation.dto.ReconciliationResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ReconciliationService {
    ReconciliationResponseDto upload(MultipartFile file1, MultipartFile file2);
}
