package com.gianluca.serviceIF;



import com.gianluca.dto.DbValidationRequest;
import com.gianluca.model.CsvValidationResponse;

public interface DbValidationService {

	
	CsvValidationResponse validateFromDatabase(DbValidationRequest request);
	
}
