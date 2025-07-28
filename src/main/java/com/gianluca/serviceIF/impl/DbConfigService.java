package com.gianluca.serviceIF.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gianluca.model.DbConfig;
import com.gianluca.repositoryIF.DbConfigRepository;

@Service
public class DbConfigService {

	@Autowired
	private DbConfigRepository dbConfigRepository;

	// Salva o aggiorna un alias
	public void saveConfig(DbConfig config) {
		dbConfigRepository.save(config);
	}

	// Recupera la configurazione di un alias specifico
	public DbConfig getConfigByAlias(String alias) {
		return dbConfigRepository.findById(alias)
				.orElseThrow(() -> new RuntimeException("Alias '" + alias + "' non trovato"));
	}

	// Restituisce tutti gli alias disponibili
	public List<String> getAllAliases() {
		return dbConfigRepository.findAll().stream().map(DbConfig::getAlias).toList();
	}
}