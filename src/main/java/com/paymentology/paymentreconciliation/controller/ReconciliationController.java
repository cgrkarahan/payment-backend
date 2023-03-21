package com.paymentology.paymentreconciliation.controller;


import com.paymentology.paymentreconciliation.dto.ReconciliationResponseDto;
import com.paymentology.paymentreconciliation.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;


    @PostMapping("/upload")
    public ResponseEntity<ReconciliationResponseDto> upload(@RequestBody MultipartFile file1,
                                                            @RequestBody MultipartFile file2) {

        return new ResponseEntity<>(reconciliationService.upload(file1, file2), HttpStatus.OK);
    }


}
