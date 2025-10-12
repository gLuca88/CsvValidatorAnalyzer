package com.gianluca.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class CsvRowError {   

	private int rowNumber;
	private String message;
	public CsvRowError(int rowNumber, String message) {
		super();
		this.rowNumber = rowNumber;
		this.message = message;
	}
	public int getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
