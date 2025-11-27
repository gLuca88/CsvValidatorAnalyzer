package com.gianluca.security;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "username obbligatorio")
	private String username;

	@NotBlank(message = "password obbligatoria")
	private String password;
}