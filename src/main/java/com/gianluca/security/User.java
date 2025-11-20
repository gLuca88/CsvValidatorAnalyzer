package com.gianluca.security;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password; // Sarà criptata (es. con BCrypt)

	@Column(nullable = false)
	private String role; // Esempio: "ADMIN", "EMPLOYEE"
	
	@Builder.Default
	@Column(nullable = false)
	private boolean protectedAdmin = false;

	
	
	
	
}
