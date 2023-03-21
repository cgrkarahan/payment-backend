package com.paymentology.paymentreconciliation.service;

import com.paymentology.paymentreconciliation.dto.ReconciliationResponseDto;
import com.paymentology.paymentreconciliation.exception.BadRequestException;
import com.paymentology.paymentreconciliation.exception.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceImplTest {

    @InjectMocks
    private ReconciliationServiceImpl reconciliationService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void contextLoads() {
    }

    @Test
    void testUploadEmptyFileFail(){
        // Create empty files
        MockMultipartFile emptyFile1 = new MockMultipartFile("file1", "", "text/csv", new byte[]{});
        MockMultipartFile emptyFile2 = new MockMultipartFile("file2", "", "text/csv", new byte[]{});

        Exception exception = assertThrows(BadRequestException.class, () ->
                reconciliationService.upload(emptyFile1, emptyFile2));

        // Verify that the correct exception was thrown
        assertEquals("Invalid file format. Please provide two CSV files.", exception.getMessage());
    }

    @Test
    void testFileUploadSuccess() throws IOException {

        int firstFileRecordCount = 14;
        int firstFileUnmatchedRecordCount = 2;
        int secondFileRecordCount = 29;
        int secondFileUnmatchedRecordCount = 17;
        int matchedRecordCount = 12;

        // Load test files
        ClassPathResource resource1 = new ClassPathResource("file1_test.csv");
        ClassPathResource resource2 = new ClassPathResource("file2_test.csv");

        byte[] content1 = Files.readAllBytes(Paths.get(resource1.getURI()));
        byte[] content2 = Files.readAllBytes(Paths.get(resource2.getURI()));

        // Create mock files
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1_test.csv", "text/csv", content1);
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2_test.csv", "text/csv", content2);

        ReconciliationResponseDto actual = reconciliationService.upload(file1,file2);

        assertEquals(firstFileRecordCount,actual.getFirstFileTotalRecordCount());
        assertEquals(firstFileUnmatchedRecordCount,actual.getFirstFileUnmatchedRecordCount());
        assertEquals(secondFileRecordCount,actual.getSecondFileTotalRecordCount());
        assertEquals(secondFileUnmatchedRecordCount,actual.getSecondFileUnmatchedRecordCount());
        assertEquals(matchedRecordCount,actual.getMatchedRecordCount());

    }

    @Test
    void testFileUploadInvalidContentFail() throws IOException {
        ClassPathResource resource1 = new ClassPathResource("file1_invalid_format.csv");
        ClassPathResource resource2 = new ClassPathResource("file2.csv");

        byte[] content1 = Files.readAllBytes(Paths.get(resource1.getURI()));
        byte[] content2 = Files.readAllBytes(Paths.get(resource2.getURI()));

        // Create mock files
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1_invalid_format.csv", "text/csv", content1);
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", content2);

        Exception exception = assertThrows(BadRequestException.class, () ->
                reconciliationService.upload(file1, file2));

        assertEquals("Parsing file exception. File name: file1_invalid_format.csv, record number:" +
                " 1. Exception: For input string: \"-20000;*MOLEPS ATM25             MOLEPOLOLE    BW\"", exception.getMessage());
    }


    @Test
    void testFileUploadEmptyContentFail() throws IOException {
        ClassPathResource resource1 = new ClassPathResource("file1_invalid_format.csv");
        ClassPathResource resource2 = new ClassPathResource("file2.csv");

        byte[] content1 = Files.readAllBytes(Paths.get(resource1.getURI()));
        byte[] content2 = Files.readAllBytes(Paths.get(resource2.getURI()));

        // Create mock files
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1_invalid_format.csv", "text/csv", new byte[]{});
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", content2);

        Exception exception = assertThrows(FileNotFoundException.class, () ->
                reconciliationService.upload(file1, file2));

        assertEquals("Please provide two file content",exception.getMessage());
    }
}