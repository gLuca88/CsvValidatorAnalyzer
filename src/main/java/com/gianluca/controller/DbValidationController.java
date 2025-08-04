package com.gianluca.controller;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gianluca.dto.DbValidationRequest;
import com.gianluca.model.CsvValidationResponse;
import com.gianluca.model.DbConfig;
import com.gianluca.serviceIF.DbValidationService;
import com.gianluca.serviceIF.impl.DbConfigService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/db")
@Slf4j
public class DbValidationController {

	@Autowired
	private DbValidationService dbValidationService;

	@Autowired
	private DbConfigService dbConfigService;

	// Endpoint POST per validare dati da un database
	@PostMapping("/validate")
	public CsvValidationResponse validateFromDatabase(@RequestBody DbValidationRequest request) {
		log.debug("Invocazione validateFromDatabase con request={}", request);
		return dbValidationService.validateFromDatabase(request);
	}

	@GetMapping("/tables/{alias}")
	public ResponseEntity<List<String>> listTables(@PathVariable String alias) {
		log.debug("Chiamata listTables per alias='{}'", alias);
		DbConfig config = dbConfigService.getConfigByAlias(alias);

		List<String> tables = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(config.getUrl(), config.getUsername(),
				config.getPassword())) {
			String schema = conn.getCatalog(); // prende solo il database specificato
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getTables(schema, null, "%", new String[] { "TABLE" });

			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME"));
			}
			log.info("Trovate {} tabelle per alias='{}'", tables.size(), alias);
			return ResponseEntity.ok(tables);
		} catch (Exception e) {
			log.error("Errore retrieving tables for alias='{}': {}", alias, e.getMessage(), e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// per dati db employee
	@GetMapping("/aliases")
	@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
	public ResponseEntity<List<String>> getAvailableDbAliases() {
		log.debug("Invocazione getAvailableDbAliases");
		List<String> aliases = dbConfigService.getAllAliases();
		log.info("Alias disponibili: {}", aliases);
		return ResponseEntity.ok(aliases);
	}
}
