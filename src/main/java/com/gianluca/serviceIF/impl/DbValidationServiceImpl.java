package com.gianluca.serviceIF.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gianluca.dto.DbValidationRequest;
import com.gianluca.model.CsvValidationResponse;
import com.gianluca.model.DbConfig;
import com.gianluca.serviceIF.CsvExportService;
import com.gianluca.serviceIF.CsvService;
import com.gianluca.serviceIF.DbValidationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbValidationServiceImpl implements DbValidationService {

	@Autowired
	private DbConfigService dbConfigService;

	@Autowired
	private CsvService csvService;

	@Autowired
	private CsvExportService csvExportService;

	@Override
	public CsvValidationResponse validateFromDatabase(DbValidationRequest request) {
		log.debug("Invocazione validateFromDatabase con dbAlias='{}', tableName='{}'", request.getDbAlias(),
				request.getTableName());
		List<String> csvLines = new ArrayList<>();
		List<String> headers = new ArrayList<>();

		try {
			// Recupera config dal dbAlias
			DbConfig config = dbConfigService.getConfigByAlias(request.getDbAlias());
			log.debug("Recuperata configurazione DB per alias='{}': url={}", request.getDbAlias(), config.getUrl());

			try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(),
					config.getPassword())) {
				log.debug("Connessione al DB stabilita");

				String query = "SELECT * FROM " + request.getTableName();
				log.debug("Esecuzione query: {}", query);
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				ResultSetMetaData meta = rs.getMetaData();

				int columnCount = meta.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					headers.add(meta.getColumnName(i));
				}

				csvLines.add(String.join(",", headers));

				while (rs.next()) {
					List<String> values = new ArrayList<>();
					for (int i = 1; i <= columnCount; i++) {
						String value = rs.getString(i);
						values.add(value != null ? value : "");
					}
					csvLines.add(String.join(",", values));
				}
				log.info("righe dalla tabella '{}'", request.getTableName());

			}
		} catch (Exception e) {
			log.error("Errore durante validateFromDatabase: {}", e.getMessage(), e);
			CsvValidationResponse errorResponse = new CsvValidationResponse();
			errorResponse.setErrors(
					List.of(new com.gianluca.model.CsvRowError(0, "Errore durante connessione DB: " + e.getMessage())));
			return errorResponse;
		}

		CsvValidationResponse response = csvService.validateCsvLines(csvLines);
		response.setCsvLines(csvLines);
		log.info("Validazione completata: validRows={}, invalidRows={}", response.getValidRows(),
				response.getInvalidRows());
		log.debug("Esporto CSV dei dati validati");
		csvExportService.exportToCsv(csvLines, request.getTableName());
		log.debug("Esporto report di validazione");
		csvExportService.exportToReport(response, request.getTableName());

		return response;
	}

}
