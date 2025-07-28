package com.gianluca.serviceIF;

import java.io.File;
import java.util.List;

import com.gianluca.model.CsvValidationResponse;

public interface CsvExportService {
	
	File exportToCsv(List<String> csvLines, String fileName);
    File exportToReport(CsvValidationResponse response, String fileName);

}
