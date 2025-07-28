package com.gianluca.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gianluca.dto.CsvMergeRequest;
import com.gianluca.serviceIF.CsvMergeService;

@RestController
@RequestMapping("/api/csv")
public class CsvMergeController {

	@Autowired
	private CsvMergeService csvMergeService;

	@PostMapping("/merge")
	public ResponseEntity<String> mergeCsvFiles(@RequestParam("csvFiles") MultipartFile[] csvFiles,
			@RequestParam("joinKey") String joinKey) {
		CsvMergeRequest request = new CsvMergeRequest();
		request.setCsvFiles(java.util.Arrays.asList(csvFiles));
		request.setJoinKey(joinKey);

		String resultPath = csvMergeService.mergeCsvFilesAndExport(request);

		return ResponseEntity.ok("File Excel creato in: " + resultPath);
	}

}
