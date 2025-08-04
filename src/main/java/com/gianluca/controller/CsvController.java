package com.gianluca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gianluca.model.CsvValidationResponse;
import com.gianluca.serviceIF.CsvService;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/csv")
@Slf4j
public class CsvController {

	private final CsvService csvService;

	public CsvController(CsvService cs) {
		this.csvService = cs;
		log.debug("Inizializzato CsvController con CsvService={}", cs.getClass().getSimpleName());
	}

	@PostMapping("/validate")
	public ResponseEntity<CsvValidationResponse> validateCsv(@RequestParam("file") MultipartFile file) {
		log.debug("Ricevuto file CSV per validazione: {}", file.getOriginalFilename());
		CsvValidationResponse response = csvService.validateCsvFile(file);
		log.info("Validazione CSV completata: valid={} errorsCount={}", 
                response.getErrors() != null ? response.getErrors().size() : 0);
		return ResponseEntity.ok(response);
	}

}
