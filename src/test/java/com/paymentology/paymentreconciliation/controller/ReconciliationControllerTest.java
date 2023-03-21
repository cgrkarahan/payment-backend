package com.paymentology.paymentreconciliation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentology.paymentreconciliation.dto.ReconciliationResponseDto;

import com.paymentology.paymentreconciliation.exception.BadRequestException;
import com.paymentology.paymentreconciliation.service.ReconciliationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReconciliationController.class)
class ReconciliationControllerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mvc;

    private ReconciliationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = ReconciliationResponseDto.builder()
                .firstFileName("file1.csv")
                .firstFileUnmatchedRecordCount(5)
                .firstFileTotalRecordCount(10)
                .matchedRecordCount(5)
                .firstFileUnmatchedRecordList(new ArrayList<>())
                .secondFileName("file2.csv")
                .secondFileTotalRecordCount(12)
                .secondFileUnmatchedRecordCount(7)
                .build();
    }

    @MockBean
    private ReconciliationService reconciliationService;

    @InjectMocks
    private ReconciliationController reconciliationController;

    @Test
    public void testUpload() throws Exception {
        ClassPathResource resource = new ClassPathResource("file1.csv");
        byte[] content = Files.readAllBytes(Paths.get(resource.getURI()));



        // Create the MockMultipartFile
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1.csv", "text/csv", content);


        resource = new ClassPathResource("file2.csv");
        byte[] content2 = Files.readAllBytes(Paths.get(resource.getURI()));
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", content2);


        when(reconciliationService.upload(Mockito.any(MultipartFile.class),Mockito.any(MultipartFile.class))).thenReturn(responseDto);

        MvcResult resultActions = mvc.perform(MockMvcRequestBuilders
                .multipart("/api/v1/upload")
                .file(file1)
                .file(file2)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.matchedRecordCount").value("5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstFileTotalRecordCount").value("10"))
                .andReturn();

        String responseString = resultActions.getResponse().getContentAsString();
        ReconciliationResponseDto responseDto = new ObjectMapper().readValue(responseString, ReconciliationResponseDto.class);

        assertEquals("file1.csv", responseDto.getFirstFileName());
        assertEquals(5, responseDto.getFirstFileUnmatchedRecordCount());
        assertEquals(10, responseDto.getFirstFileTotalRecordCount());
        assertEquals(5, responseDto.getMatchedRecordCount());
        assertEquals("file2.csv", responseDto.getSecondFileName());
        assertEquals(12, responseDto.getSecondFileTotalRecordCount());
        assertEquals(7, responseDto.getSecondFileUnmatchedRecordCount());


    }

    @Test
    public void testUploadInvalidFileFormat() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1.txt", "text/plain", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.txt", "text/plain", "test data".getBytes());

        when(reconciliationService.upload(Mockito.any(MultipartFile.class), Mockito.any(MultipartFile.class)))
                .thenThrow(new BadRequestException("Invalid file type"));

        MvcResult resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/upload")
                .file(file1)
                .file(file2)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUploadWithInvalidContent() throws Exception {


        ClassPathResource resource = new ClassPathResource("file1_invalid_format.csv");
        byte[] content = Files.readAllBytes(Paths.get(resource.getURI()));
        MockMultipartFile file1 = new MockMultipartFile("file1", "file1_invalid_format.csv", "text/csv", content);

        resource = new ClassPathResource("file2.csv");
        byte[] content2 = Files.readAllBytes(Paths.get(resource.getURI()));
        MockMultipartFile file2 = new MockMultipartFile("file2", "file2.csv", "text/csv", content2);

        when(reconciliationService.upload(Mockito.any(MultipartFile.class), Mockito.any(MultipartFile.class)))
                .thenThrow(new BadRequestException("Invalid file type"));

        mvc.perform(MockMvcRequestBuilders
                .multipart("/api/v1/upload")
                .file(file1)
                .file(file2)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}