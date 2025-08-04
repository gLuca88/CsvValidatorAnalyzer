package com.gianluca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gianluca.model.DbConfig;
import com.gianluca.serviceIF.impl.DbConfigService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/dbconfig")
@Slf4j
public class DbConfigAdminController {

    @Autowired
    private DbConfigService dbConfigService;

    // 🔐 Registra o aggiorna un alias DB
    @PostMapping("/register")
    public ResponseEntity<String> registerDb(@RequestBody DbConfig config) {
    	log.debug("Register/Update DB alias: {}", config.getAlias());
        dbConfigService.saveConfig(config);
        log.info("Alias '{}' registrato con successo", config.getAlias());
        return ResponseEntity.ok("Alias '" + config.getAlias() + "' registrato con successo.");
    }

    // 🔐 Elenco di tutti gli alias disponibili
    @GetMapping("/list")
    public List<String> listAllDbAliases() {
    	List<String> aliases = dbConfigService.getAllAliases();
    	log.debug("Chiamata a listAllDbAliases");
    	log.info("Recuperati {} alias DB", aliases.size());
        return dbConfigService.getAllAliases();
        
    }
}
