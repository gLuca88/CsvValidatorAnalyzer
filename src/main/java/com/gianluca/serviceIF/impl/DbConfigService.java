package com.gianluca.serviceIF.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gianluca.model.DbConfig;
import com.gianluca.repositoryIF.DbConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbConfigService {

	@Autowired
	private DbConfigRepository dbConfigRepository;

	// Salva o aggiorna un alias
	public void saveConfig(DbConfig config) {
		log.debug("Salvataggio/aggiornamento DB config per alias='{}'", config.getAlias());
		dbConfigRepository.save(config);
		log.info("DB config salvata per alias='{}'", config.getAlias());
	}

	// Recupera la configurazione di un alias specifico
	public DbConfig getConfigByAlias(String alias) {
		log.debug("Recupero DB config per alias='{}'", alias);
		return dbConfigRepository.findById(alias)
				.orElseThrow(() -> new RuntimeException("Alias '" + alias + "' non trovato"));

	}

	// Restituisce tutti gli alias disponibili
	public List<String> getAllAliases() {
		log.debug("Recupero di tutti gli alias DB");
		return dbConfigRepository.findAll().stream().map(DbConfig::getAlias).toList();
	}
}