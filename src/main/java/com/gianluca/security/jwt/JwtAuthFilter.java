package com.gianluca.security.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private static final Set<String> PUBLIC_PATHS = Set.of("/api/auth/login", "/api/auth/register",
			"/api/auth/is-empty");

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (isPublic(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String token = authHeader.substring(7);

		try {
			if (!jwtService.isTokenValid(token)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			String username = jwtService.extractUsername(token);
			String role = jwtService.extractRole(token);

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
					Collections.singleton(() -> "ROLE_" + role));

			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);

		} catch (ExpiredJwtException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;

		} catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isPublic(HttpServletRequest request) {
		String path = request.getServletPath();

		if (path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") || path.equals("/favicon.ico")) {
			return true;
		}

		return PUBLIC_PATHS.contains(path);
	}
}
