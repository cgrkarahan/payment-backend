package com.paymentology.paymentreconciliation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReconciliationDto implements Comparable<ReconciliationDto> {

    final int COEFFICIENT_TRANSACTION_ID = 6;
    final int COEFFICIENT_TRANSACTION_DATE = 5;
    final int COEFFICIENT_TRANSACTION_TIME_DIFFERENCE = 3;
    final int COEFFICIENT_TRANSACTION_DAY_DIFFERENCE = 2;
    final int COEFFICIENT_TRANSACTION_DESCRIPTION = 2;
    final int COEFFICIENT_TRANSACTION_AMOUNT = 4;
    final int COEFFICIENT_TRANSACTION_TYPE = 1;
    final int COEFFICIENT_WALLET_REFERENCE = 4;
    final int COEFFICIENT_TRANSACTION_NARRATIVE = 2;
    final int COEFFICIENT_PROFILE_NAME = 1;


    private String transactionId;
    private String transactionType;
    private String transactionDescription;
    private String transactionNarrative;
    private long transactionAmount;
    private LocalDateTime transactionDate;
    private String profileName;
    private String walletReference;
    private String filename;
    private double similarityScore;
    private String status;

    @Override
    public int compareTo(ReconciliationDto reconciliationDto) {
        if (reconciliationDto.getTransactionId() == null || this.getTransactionId() == null)
            return 0;
        return this.getTransactionId().compareTo(reconciliationDto.getTransactionId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReconciliationDto that = (ReconciliationDto) o;
        return transactionAmount == that.transactionAmount &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(transactionType, that.transactionType) &&
                Objects.equals(transactionDescription, that.transactionDescription) &&
                Objects.equals(transactionNarrative, that.transactionNarrative) &&
                Objects.equals(transactionDate, that.transactionDate) &&
                Objects.equals(profileName, that.profileName) &&
                Objects.equals(walletReference, that.walletReference);
    }

    public double calculateSimilarity(ReconciliationDto reconciliationDto) {


        int numMatchingFields = 0; // number of fields that match
        if (transactionId.equals(reconciliationDto.transactionId)) {
            numMatchingFields += COEFFICIENT_TRANSACTION_ID;
        }
        if (transactionDate.equals(reconciliationDto.transactionDate)) {

            numMatchingFields += COEFFICIENT_TRANSACTION_DATE;
        } else if (transactionDate.toLocalDate().equals(reconciliationDto.transactionDate.minusDays(1)) ||
                transactionDate.toLocalDate().equals(reconciliationDto.transactionDate.toLocalDate())) {
            numMatchingFields += COEFFICIENT_TRANSACTION_TIME_DIFFERENCE;

        } else if (transactionDate.equals(reconciliationDto.transactionDate.minusDays(1)) ||
                transactionDate.equals(reconciliationDto.transactionDate.plusDays(1))) {
            numMatchingFields += COEFFICIENT_TRANSACTION_DAY_DIFFERENCE;

        }
        if (transactionDescription.equals(reconciliationDto.transactionDescription)) {
            numMatchingFields += COEFFICIENT_TRANSACTION_DESCRIPTION;
        }
        if (transactionAmount == reconciliationDto.transactionAmount) {
            numMatchingFields += COEFFICIENT_TRANSACTION_AMOUNT;
        }
        if (transactionType.equals(reconciliationDto.transactionType)) {
            numMatchingFields += COEFFICIENT_TRANSACTION_TYPE;
        }
        if (walletReference.equals(reconciliationDto.walletReference)) {
            numMatchingFields += COEFFICIENT_WALLET_REFERENCE;
        }
        if (transactionNarrative.equals(reconciliationDto.transactionNarrative)) {
            numMatchingFields += COEFFICIENT_TRANSACTION_NARRATIVE;
        }
        if (profileName.equals(reconciliationDto.profileName)) {
            numMatchingFields += COEFFICIENT_PROFILE_NAME;
        }

        double similarityScore = ((double) numMatchingFields / 25) * 100;
        return similarityScore;
    }

}
