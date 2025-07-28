package com.gianluca.serviceIF;

import com.gianluca.dto.CsvMergeRequest;

public interface CsvMergeService {

	String mergeCsvFilesAndExport(CsvMergeRequest request);
}
