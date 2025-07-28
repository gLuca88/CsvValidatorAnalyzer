package com.gianluca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gianluca.model.CsvValidationResponse;
import com.gianluca.serviceIF.CsvService;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

	private final CsvService csvService;

	public CsvController(CsvService cs) {
		this.csvService = cs;
	}

	@PostMapping("/validate")
	public ResponseEntity<CsvValidationResponse> validateCsv(@RequestParam("file") MultipartFile file){
		CsvValidationResponse response = csvService.validateCsvFile(file);
        return ResponseEntity.ok(response);
	}
	
	
}
