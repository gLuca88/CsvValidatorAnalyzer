package com.gianluca.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CsvMergeRequest {
	
	 // I file CSV da unire
    private List<MultipartFile> csvFiles;

    // Nome della colonna chiave da usare per collegare i file
    private String joinKey;

}
