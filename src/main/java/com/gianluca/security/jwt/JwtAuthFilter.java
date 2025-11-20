package com.gianluca.security.jwt;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getServletPath();

		// 🔓 Pagine e API pubbliche
		if (path.equals("/login.html") || path.equals("/register-admin.html") || path.equals("/index.html")
				|| path.equals("/style.css") || path.equals("/main.js") || path.equals("/favicon.ico")
				|| path.startsWith("/api/auth/login") || path.startsWith("/api/auth/is-empty")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 🔑 Header Authorization
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		// 🔍 Validazione token con nuova API
		if (jwtService.isTokenValid(token)) {
			String username = jwtService.extractUsername(token);
			String role = jwtService.extractRole(token);

			// 🔐 Costruzione Authentication
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
					Collections.singleton(() -> "ROLE_" + role));

			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		filterChain.doFilter(request, response);
	}
}
