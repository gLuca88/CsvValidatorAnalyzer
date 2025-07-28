package com.gianluca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "db_config")
@Data
public class DbConfig {

	@Id
	private String alias; // es: "clienteA", "archivio2025"

	private String url; // es: jdbc:mysql://localhost:3306/dbcliente
	private String username;
	private String password;
}