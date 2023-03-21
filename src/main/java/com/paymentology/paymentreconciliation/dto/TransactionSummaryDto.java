package com.paymentology.paymentreconciliation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TransactionSummaryDto {

    private Set<ReconciliationDto> fileOneMatchedRecords;
    private Set<ReconciliationDto> fileTwoMatchedRecords;
    private List<ReconciliationDto> fileOneUnMatchedRecord;
    private List<ReconciliationDto> fileTwoUnMatchedRecord;

}
