package com.paymentology.paymentreconciliation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconciliationResponseDto {

    private int firstFileTotalRecordCount;
    List<ReconciliationDto> firstFileUnmatchedRecordList;
    private int firstFileUnmatchedRecordCount;
    private String firstFileName;
    private int secondFileTotalRecordCount;
    List<ReconciliationDto> secondFileUnmatchedRecordList;
    private int secondFileUnmatchedRecordCount;
    private String secondFileName;
    private int matchedRecordCount;

}
