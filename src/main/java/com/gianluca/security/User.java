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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isProtectedAdmin() {
		return protectedAdmin;
	}

	public void setProtectedAdmin(boolean protectedAdmin) {
		this.protectedAdmin = protectedAdmin;
	}
	
	
	
}
