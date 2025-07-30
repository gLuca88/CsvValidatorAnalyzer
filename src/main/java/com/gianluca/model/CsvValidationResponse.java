package com.gianluca.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvValidationResponse {

	private int totalRows;
	private int validRows;
	private int invalidRows;
	private List<CsvRowError> errors;

	private List<String> headers;
	private List<String> inferredRequiredColumns;// -->La lista delle colonne che non hanno nessun valore mancante
	private Map<String, Integer> columnNullCounts;// -->Una mappa che dice, per ogni colonna, quanti valori mancanti ci
													// sono.

	// -->Aggiunto per permettere l'esportazione
	private List<String> csvLines;

	// Costruttore usato dalla convalida CSV, senza campo csvLines
	public CsvValidationResponse(int totalRows, int validRows, int invalidRows, List<CsvRowError> errors,
			List<String> headers, List<String> inferredRequiredColumns, Map<String, Integer> columnNullCounts) {
		this.totalRows = totalRows;
		this.validRows = validRows;
		this.invalidRows = invalidRows;
		this.errors = errors;
		this.headers = headers;
		this.inferredRequiredColumns = inferredRequiredColumns;
		this.columnNullCounts = columnNullCounts;
	}

}
