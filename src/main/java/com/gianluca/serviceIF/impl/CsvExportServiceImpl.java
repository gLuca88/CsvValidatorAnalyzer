package com.gianluca.serviceIF.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.gianluca.model.CsvRowError;
import com.gianluca.model.CsvValidationResponse;
import com.gianluca.serviceIF.CsvExportService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvExportServiceImpl implements CsvExportService {

	private String getDesktopPath() {
		return System.getProperty("user.home") + File.separator + "Desktop";
	}

	@Override
	public File exportToCsv(List<String> csvLines, String tableName) {
		log.debug("Inizio exportToCsv: tableName='{}', linesCount={}", tableName, csvLines.size());
		try {
			// Percorso: Desktop/csv_generati
			String dirPath = Paths.get(getDesktopPath(), "csv_generati").toString();
			File dir = new File(dirPath);
			if (!dir.exists())
				log.debug("Directory non esistente, la creo: {}", dirPath);
			dir.mkdirs();

			// Nome file: nomeTabella.csv
			File file = new File(dir, tableName + ".csv");
			log.debug("Creazione FileWriter per file: {}", file.getAbsolutePath());
			FileWriter writer = new FileWriter(file);

			for (String line : csvLines) {
				writer.write(line + "\n");
			}

			writer.close();
			log.info("CSV esportato con successo in: {}", file.getAbsolutePath());
			return file;
		} catch (IOException e) {
			log.error("Errore durante esportazione CSV per table '{}': {}", tableName, e.getMessage(), e);
			throw new RuntimeException("Errore durante esportazione CSV", e);
		}
	}

	@Override
	public File exportToReport(CsvValidationResponse response, String tableName) {
		log.debug("Inizio exportToReport: tableName='{}', totalRows={}", tableName, response.getTotalRows());
		try {
			// Percorso: Desktop/report_csv
			String dirPath = Paths.get(getDesktopPath(), "report_csv").toString();
			File dir = new File(dirPath);
			if (!dir.exists())
				log.debug("Directory non esistente, la creo: {}", dirPath);
			dir.mkdirs();

			// Nome file: report_nomeTabella.txt
			File file = new File(dir, "report_" + tableName + ".txt");
			log.debug("Creazione FileWriter per report: {}", file.getAbsolutePath());
			FileWriter writer = new FileWriter(file);

			writer.write("🔍 Report Validazione CSV\n\n");
			writer.write("Totale righe: " + response.getTotalRows() + "\n");
			writer.write("Righe valide: " + response.getValidRows() + "\n");
			writer.write("Righe non valide: " + response.getInvalidRows() + "\n\n");

			if (!response.getErrors().isEmpty()) {
				writer.write("❌ Errori trovati:\n");
				for (CsvRowError error : response.getErrors()) {
					writer.write("- Riga " + error.getRowNumber() + ": " + error.getMessage() + "\n");
				}
				writer.write("\n");
			}

			writer.write(
					"📌 Colonne obbligatorie: " + String.join(", ", response.getInferredRequiredColumns()) + "\n\n");

			writer.write("📊 Valori nulli per colonna:\n");
			for (Map.Entry<String, Integer> entry : response.getColumnNullCounts().entrySet()) {
				writer.write("- " + entry.getKey() + ": " + entry.getValue() + "\n");
			}

			writer.close();
			log.info("Report CSV esportato con successo in: {}", file.getAbsolutePath());
			return file;
		} catch (IOException e) {
			throw new RuntimeException("Errore durante esportazione report", e);
		}
	}

}
