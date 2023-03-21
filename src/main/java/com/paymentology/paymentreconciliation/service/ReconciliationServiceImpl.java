package com.paymentology.paymentreconciliation.service;

import com.paymentology.paymentreconciliation.dto.ReconciliationDto;
import com.paymentology.paymentreconciliation.dto.ReconciliationResponseDto;
import com.paymentology.paymentreconciliation.dto.TransactionSummaryDto;
import com.paymentology.paymentreconciliation.exception.BadRequestException;
import com.paymentology.paymentreconciliation.exception.FileNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ReconciliationServiceImpl implements ReconciliationService {


    /**

     Uploads and reconciles two CSV files, and returns the reconciliation result.
     The method first checks the content and extension of the provided files, then converts them to a list of
     ReconciliationDto objects. After that, the reconcileTransactions method is called to reconcile the two lists of
     transactions, and the result is returned in a ReconciliationResponseDto object that contains the total count and
     unmatched count of each file, the matched record count, and the unmatched record lists of each file.
     @param file1 the first CSV file to be reconciled
     @param file2 the second CSV file to be reconciled
     @return a ReconciliationResponseDto object that contains the reconciliation result
     @throws BadRequestException if there is an error in checking files' content or extension, or parsing files
     */
    @Override
    public ReconciliationResponseDto upload(MultipartFile file1, MultipartFile file2) {


        checkFilesContentAndExtension(file1, file2);

        // Convert MultipartFile objects to TransactionDto objects
        List<ReconciliationDto> originalFileOne = convertFileToModel(file1);
        List<ReconciliationDto> originalFileTwo = convertFileToModel(file2);


        TransactionSummaryDto transactionSummaryDto = reconcileTransactions(originalFileOne, originalFileTwo);

        return ReconciliationResponseDto.builder()
                .firstFileTotalRecordCount(originalFileOne.size())
                .firstFileUnmatchedRecordCount(transactionSummaryDto.getFileOneUnMatchedRecord().size())
                .firstFileName(file1.getOriginalFilename())
                .secondFileTotalRecordCount(originalFileTwo.size())
                .secondFileUnmatchedRecordCount(transactionSummaryDto.getFileTwoUnMatchedRecord().size())
                .secondFileName(file2.getOriginalFilename())
                .matchedRecordCount(transactionSummaryDto.getFileOneMatchedRecords().size())
                .firstFileUnmatchedRecordList(transactionSummaryDto.getFileOneUnMatchedRecord())
                .secondFileUnmatchedRecordList(transactionSummaryDto.getFileTwoUnMatchedRecord())
                .build();
    }

    /**
     * Checks the content and extension of two CSV files.
     * <p>
     * The method checks that both files have a ".csv" extension and that their content is not empty. If any of these
     * conditions is not met, the method throws an exception with a descriptive error message.
     *
     * @param file1 the first file to check
     * @param file2 the second file to check
     * @throws BadRequestException if either file does not have a ".csv" extension
     * @throws FileNotFoundException if either file is empty
     */
    private void checkFilesContentAndExtension(MultipartFile file1, MultipartFile file2) {
        // Check file extensions
        if (!file1.getOriginalFilename().endsWith(".csv") || !file2.getOriginalFilename().endsWith(".csv")) {
            throw new BadRequestException("Invalid file format. Please provide two CSV files.");
        }

        // Check file content
        if (file1.isEmpty() || file2.isEmpty()) {
            throw new FileNotFoundException("Please provide two file content");
        }
    }

    /**

     Reconciles two lists of {@link ReconciliationDto} objects to find matched and unmatched transactions.
     @param fileOne the first list of transactions to reconcile
     @param fileTwo the second list of transactions to reconcile
     @return a {@link TransactionSummaryDto} object containing lists of matched and unmatched transactions
     */
    private TransactionSummaryDto reconcileTransactions(List<ReconciliationDto> fileOne, List<ReconciliationDto> fileTwo) {
        Map<String, List<ReconciliationDto>> fileOneMap = new HashMap<>();
        Map<String, List<ReconciliationDto>> fileTwoMap = new HashMap<>();

        Set<ReconciliationDto> matched = new HashSet<>();

        List<ReconciliationDto> unmatched1 = new ArrayList<>();
        List<ReconciliationDto> unmatched2 = new ArrayList<>();

        // Populate fileOneMap with records from fileOne
        for (ReconciliationDto transaction : fileOne) {
            String key = transaction.getTransactionId();
            fileOneMap.computeIfAbsent(key, k -> new ArrayList<>()).add(transaction);
        }

        // Populate fileTwoMap with records from table 2
        for (ReconciliationDto transaction : fileTwo) {
            String key = transaction.getTransactionId();
            fileTwoMap.computeIfAbsent(key, k -> new ArrayList<>()).add(transaction);
        }

        // Find matched transactions
        for (String key : fileOneMap.keySet()) {
            List<ReconciliationDto> transactionsListOne = fileOneMap.get(key);
            List<ReconciliationDto> transactionListTwo = fileTwoMap.get(key);

            if (transactionListTwo != null) {
                for (ReconciliationDto transaction1 : transactionsListOne) {
                    boolean foundMatch = false;
                    for (ReconciliationDto transaction2 : transactionListTwo) {
                        double similarity = transaction1.calculateSimilarity(transaction2);
                        transaction1.setSimilarityScore(similarity);
                        if (similarity > 90) {
                            foundMatch = true;
                            matched.add(transaction1);
                            transactionListTwo.remove(transaction2);
                            break;
                        }
                    }
                    if (!foundMatch) {
                        unmatched1.add(transaction1);
                    }
                }
                if (!transactionListTwo.isEmpty()) {
                    unmatched2.addAll(transactionListTwo);
                }
                fileTwoMap.remove(key);
            } else {
                unmatched1.addAll(transactionsListOne);
            }
        }

        // Add remaining unmatched records from table2Map to unmatched2 list
        unmatched2.addAll(fileTwoMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));


        TransactionSummaryDto transactionSummaryDto = new TransactionSummaryDto();
        transactionSummaryDto.setFileOneMatchedRecords(matched);
        transactionSummaryDto.setFileTwoMatchedRecords(matched);
        transactionSummaryDto.setFileOneUnMatchedRecord(unmatched1);
        transactionSummaryDto.setFileTwoUnMatchedRecord(unmatched2);

        return transactionSummaryDto;

    }

    /**

     Converts the given CSV file to a list of ReconciliationDto objects.
     Each line in the CSV file is converted to a ReconciliationDto object, and the list of
     these objects is returned.
     @param file The CSV file to convert
     @return An ArrayList of ReconciliationDto objects representing the data in the CSV file
     @throws BadRequestException if there is an error parsing the file
     */
    private ArrayList<ReconciliationDto> convertFileToModel(MultipartFile file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setSkipHeaderRecord(true)
                    .setDelimiter(",")
                    .setHeader()
                    .build();

            Iterable<CSVRecord> csvRecords = csvFormat.parse(reader);
            ArrayList<ReconciliationDto> transactionList = new ArrayList<>();


            for (CSVRecord csvRecord : csvRecords) {
                try {
                    ReconciliationDto transaction = ReconciliationDto.builder()
                            .transactionId(csvRecord.get("TransactionID"))
                            .transactionType(csvRecord.get("TransactionType"))
                            .transactionDescription(csvRecord.get("TransactionDescription"))
                            .transactionNarrative(csvRecord.get("TransactionNarrative"))
                            .transactionAmount(Long.valueOf(csvRecord.get("TransactionAmount")))
                            .transactionDate(LocalDateTime.parse(csvRecord.get("TransactionDate")
                                    , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .profileName(csvRecord.get("ProfileName"))
                            .walletReference(csvRecord.get("WalletReference"))
                            .filename(file.getOriginalFilename())
                            .build();

                    transactionList.add(transaction);
                } catch (Exception ex) {

                    String message = "Parsing file exception. File name: %s, record number: %s. Exception: %s"
                            .formatted(file.getOriginalFilename(), csvRecord.getRecordNumber(), ex.getMessage());
                    throw new BadRequestException(message);
                }

            }
            return transactionList;

        } catch (IOException ex) {
            throw new BadRequestException("File parsing exception" + ex.getMessage());
        }
    }
}
