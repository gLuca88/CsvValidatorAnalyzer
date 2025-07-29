package com.gianluca.security;

public interface AuthService {
	User authenticate(String username, String password);
}