package com.gianluca.repositoryIF;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gianluca.model.DbConfig;

public interface DbConfigRepository extends JpaRepository<DbConfig, String> {
	// Puoi aggiungere metodi personalizzati se servono
}
