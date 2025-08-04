package com.gianluca.serviceIF.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gianluca.dto.CsvMergeRequest;
import com.gianluca.serviceIF.CsvMergeService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvMergeServiceImpl implements CsvMergeService {

	@Override
	public String mergeCsvFilesAndExport(CsvMergeRequest request) {
		String joinKey = request.getJoinKey().trim();
		List<List<Map<String, String>>> allFilesData = new ArrayList<>();
		List<String> fileNames = new ArrayList<>();

		try {
			for (MultipartFile file : request.getCsvFiles()) {
				String origName = file.getOriginalFilename();
				log.debug("Parsing del file CSV: {}", origName);
				List<Map<String, String>> rows = new ArrayList<>();

				try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
					String line;
					List<String> headers = new ArrayList<>();
					List<Integer> validIndexes = new ArrayList<>();
					boolean isFirstLine = true;

					while ((line = br.readLine()) != null) {
						String[] parts = line.split(",");

						if (isFirstLine) {
							for (int i = 0; i < parts.length; i++) {
								String header = parts[i].trim();
								if (!header.toLowerCase().contains("password")) {
									headers.add(header);
									validIndexes.add(i);
								}
							}
							log.debug("Headers trovati (escluse password): {}", headers);
							isFirstLine = false;
						} else {
							Map<String, String> row = new LinkedHashMap<>();
							for (int i = 0; i < headers.size(); i++) {
								int colIndex = validIndexes.get(i);
								String value = (colIndex < parts.length) ? parts[colIndex].trim() : "";
								row.put(headers.get(i), value);
							}
							rows.add(row);
						}
					}
				}

				allFilesData.add(rows);

				String name = file.getOriginalFilename();
				if (name != null) {
					fileNames.add(name.replaceAll("\\.csv$", ""));
					log.debug("File '{}' parsed, righe lette: {}", origName, rows.size());
				}
			}

			if (allFilesData.isEmpty()) {
				log.warn("Nessun file CSV fornito in richiesta");
				return "Errore: nessun file CSV fornito.";
			}

			// Merge progressivo
			List<Map<String, String>> baseData = allFilesData.get(0);
			log.debug("Dati di base iniziali: {} righe", baseData.size());

			for (List<Map<String, String>> nextFile : allFilesData.subList(1, allFilesData.size())) {
				log.debug("Merge con il prossimo file ({}) righe", nextFile.size());
				Map<String, Map<String, String>> lookupMap = nextFile.stream().filter(row -> row.get(joinKey) != null)
						.collect(Collectors.toMap(row -> row.get(joinKey).trim(), row -> row,
								(existing, replacement) -> existing));

				List<Map<String, String>> mergedRows = new ArrayList<>();
				for (Map<String, String> baseRow : baseData) {
					String key = baseRow.get(joinKey);
					Map<String, String> extra = key != null ? lookupMap.get(key.trim()) : null;

					Map<String, String> merged = new LinkedHashMap<>(baseRow);
					if (extra != null) {
						for (Map.Entry<String, String> e : extra.entrySet()) {
							if (!merged.containsKey(e.getKey())) {
								merged.put(e.getKey(), e.getValue());
							}
						}
					}
					mergedRows.add(merged);
				}

				baseData = mergedRows;
				log.debug("Dopo merge progressivo, righe totali: {}", baseData.size());
			}

			// Scrittura Excel
			Set<String> finalHeaders = baseData.stream().flatMap(m -> m.keySet().stream())
					.filter(h -> !h.toLowerCase().contains("password"))
					.collect(Collectors.toCollection(LinkedHashSet::new));
			log.debug("Final headers per Excel: {}", finalHeaders);

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("MergedData");

			int rowIndex = 0;
			Row headerRow = sheet.createRow(rowIndex++);
			int colIndex = 0;
			for (String header : finalHeaders) {
				headerRow.createCell(colIndex++).setCellValue(header);
			}

			for (Map<String, String> rowMap : baseData) {
				Row row = sheet.createRow(rowIndex++);
				colIndex = 0;
				for (String header : finalHeaders) {
					row.createCell(colIndex++).setCellValue(rowMap.getOrDefault(header, ""));
				}
			}
			log.info("Workbook popolato: {} righe, {} colonne", baseData.size(), finalHeaders.size());

			String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
			Path outputDir = Paths.get(desktopPath, "file_excel_csv");
			if (!Files.exists(outputDir)) {
				Files.createDirectories(outputDir);
				log.debug("Directory creata: {}", outputDir);
			}

			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			String fileName = "merged_" + String.join("_", fileNames) + "_" + timestamp + ".xlsx";
			File outputFile = outputDir.resolve(fileName).toFile();

			try (FileOutputStream out = new FileOutputStream(outputFile)) {
				workbook.write(out);
			}
			workbook.close();
			log.info("File Excel creato con successo in: {}", outputFile.getAbsolutePath());
			return outputFile.getAbsolutePath();

		} catch (Exception e) {
			log.error("Errore durante il mergeCsvFilesAndExport: {}", e.getMessage(), e);
			e.printStackTrace();
			return "Errore durante il merge: " + e.getMessage();
		}
	}

}
