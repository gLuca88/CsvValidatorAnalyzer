package com.gianluca.serviceIF.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gianluca.model.CsvRowError;
import com.gianluca.model.CsvValidationResponse;
import com.gianluca.serviceIF.CsvService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvServImpl implements CsvService {

	@Override
	public CsvValidationResponse validateCsvFile(MultipartFile file) {
		String filename = file.getOriginalFilename();
		log.debug("Inizio validateCsvFile per file='{}'", filename);
		// Lista per memorizzare gli errori riga per riga
		List<CsvRowError> errorList = new ArrayList<>();

		// Mappa per contare i valori nulli per ogni colonna, mantenendo l'ordine
		// originale
		Map<String, Integer> columnNullCounts = new LinkedHashMap<>();

		// Lista delle intestazioni del CSV
		List<String> headersList = new ArrayList<>();

		List<String> csvLines = new ArrayList<>();// -->righe nel json di rix

		int totalRows = 0;// Numero totale di righe (esclusa intestazione)
		int validRows = 0;// Numero di righe valide (nessun campo vuoto)

		// Array per memorizzare le intestazioni, sarà inizializzato alla prima riga
		String[] headers = null;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

			String line;
			boolean isFirstLine = true;// Serve per distinguere la prima riga (intestazione)

			while ((line = reader.readLine()) != null) {
				csvLines.add(line);
				String[] parts = line.split(",");

				if (isFirstLine) {
					log.debug("Parsing header CSV: {}", line);
					headers = parts;
					for (String header : headers) {
						String trimmedHeader = header.trim();
						headersList.add(trimmedHeader);
						columnNullCounts.put(trimmedHeader, 0);// Inizialmente tutti i contatori a 0
					}
					isFirstLine = false;
					continue;// Salta il resto del ciclo per non analizzare l'intestazione come dati
				}
				totalRows++;// Conta la riga corrente

				// Se il numero di colonne non corrisponde all'intestazione, è un errore
				if (parts.length != headers.length) {
					errorList.add(new CsvRowError(totalRows,
							"Numero di colonne non valido (attesi: " + headers.length + ")"));
					log.warn("Riga {}: colonne attese={}, trovate={}", totalRows, headers.length, parts.length);
					continue;// Salta l'analisi della riga
				}

				boolean isValid = true;

				for (int i = 0; i < parts.length; i++) {
					String value = parts[i].trim();
					String column = headers[i].trim();

					if (value.isEmpty()) {
						columnNullCounts.put(column, columnNullCounts.get(column) + 1);// Incrementa il contatore di
																						// nulli per la colonna
						errorList.add(new CsvRowError(totalRows, "Campo vuoto nella colonna '" + column + "'"));
						isValid = false;
						log.debug("Riga {}: campo vuoto in '{}'", totalRows, column);
					}
				}
				if (isValid) {// Se nessun campo è vuoto, la riga è valida
					validRows++;
				}

			}

		} catch (Exception e) {// Errore generico nella lettura del file
			log.error("Errore nella lettura del file '{}': {}", filename, e.getMessage(), e);
			errorList.add(new CsvRowError(0, "Errore nella lettura del file:" + e.getMessage()));
		}

		int invalidRows = totalRows - validRows;// Calcola il numero di righe non valide come differenza tot righe-rig
												// not valid
		log.info("validateCsvFile completato per '{}': totalRows={}, validRows={}, invalidRows={}", filename, totalRows,
				validRows, invalidRows);

		// Determina colonne "obbligatorie" come quelle con zero valori nulli
		List<String> inferredRequiredColumns = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : columnNullCounts.entrySet()) {
			if (entry.getValue() == 0) {
				inferredRequiredColumns.add(entry.getKey());
			}
		}
		// Restituisce la risposta completa con tutte le statistiche in base al model
		// costruito
		return new CsvValidationResponse(totalRows, validRows, invalidRows, errorList, headersList,
				inferredRequiredColumns, columnNullCounts, csvLines);
	}

	@Override
	public CsvValidationResponse validateCsvLines(List<String> lines) {
		log.debug("Inizio validateCsvLines per {} righe", lines.size());
		List<CsvRowError> errorList = new ArrayList<>();
		Map<String, Integer> columnNullCounts = new LinkedHashMap<>();
		List<String> headersList = new ArrayList<>();
		int totalRows = 0;
		int validRows = 0;
		String[] headers = null;
		try {
			boolean isFirstLine = true;

			for (String line : lines) {
				String[] parts = line.split(",");

				if (isFirstLine) {
					log.debug("Parsing header da lista: {}", line);
					headers = parts;
					for (String header : headers) {
						String trimmedHeader = header.trim();
						headersList.add(trimmedHeader);
						columnNullCounts.put(trimmedHeader, 0);
					}
					isFirstLine = false;
					continue;
				}

				totalRows++;

				if (parts.length != headers.length) {
					errorList.add(new CsvRowError(totalRows,
							"Numero di colonne non valido (attesi: " + headers.length + ")"));
					log.warn("Riga {}: colonne attese={}, trovate={}", totalRows, headers.length, parts.length);
					continue;
				}

				boolean isValid = true;
				for (int i = 0; i < parts.length; i++) {
					String value = parts[i].trim();
					String column = headers[i].trim();
					if (value.isEmpty()) {
						columnNullCounts.put(column, columnNullCounts.get(column) + 1);
						errorList.add(new CsvRowError(totalRows, "Campo vuoto nella colonna '" + column + "'"));
						isValid = false;
						log.debug("Riga {}: campo vuoto in '{}'", totalRows, column);
					}
				}

				if (isValid) {
					validRows++;
				}
			}

		} catch (Exception e) {
			log.error("Errore nella validazione delle righe: {}", e.getMessage(), e);
			errorList.add(new CsvRowError(0, "Errore nella lettura dei dati: " + e.getMessage()));
		}

		int invalidRows = totalRows - validRows;
		log.info("validateCsvLines completato: totalRows={}, validRows={}, invalidRows={}", totalRows, validRows,
				invalidRows);

		List<String> inferredRequiredColumns = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : columnNullCounts.entrySet()) {
			if (entry.getValue() == 0) {
				inferredRequiredColumns.add(entry.getKey());
			}
		}

		return new CsvValidationResponse(totalRows, validRows, invalidRows, errorList, headersList,
				inferredRequiredColumns, columnNullCounts);
	}

}
