package com.gianluca.serviceIF;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.gianluca.model.CsvValidationResponse;

public interface CsvService {

	CsvValidationResponse validateCsvFile(MultipartFile file);
	
	CsvValidationResponse validateCsvLines(List<String> lines);
}
