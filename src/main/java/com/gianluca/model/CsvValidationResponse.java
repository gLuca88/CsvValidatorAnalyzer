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

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public int getValidRows() {
		return validRows;
	}

	public void setValidRows(int validRows) {
		this.validRows = validRows;
	}

	public int getInvalidRows() {
		return invalidRows;
	}

	public void setInvalidRows(int invalidRows) {
		this.invalidRows = invalidRows;
	}

	public List<CsvRowError> getErrors() {
		return errors;
	}

	public void setErrors(List<CsvRowError> errors) {
		this.errors = errors;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<String> getInferredRequiredColumns() {
		return inferredRequiredColumns;
	}

	public void setInferredRequiredColumns(List<String> inferredRequiredColumns) {
		this.inferredRequiredColumns = inferredRequiredColumns;
	}

	public Map<String, Integer> getColumnNullCounts() {
		return columnNullCounts;
	}

	public void setColumnNullCounts(Map<String, Integer> columnNullCounts) {
		this.columnNullCounts = columnNullCounts;
	}

	public List<String> getCsvLines() {
		return csvLines;
	}

	public void setCsvLines(List<String> csvLines) {
		this.csvLines = csvLines;
	}

}
