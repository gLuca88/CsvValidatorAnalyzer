package com.gianluca.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gianluca.dto.UserDto;
import com.gianluca.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.authService = authService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		log.debug("Initialized AuthController with AuthService={}, UserRepository={}",
				authService.getClass().getSimpleName(), userRepository.getClass().getSimpleName());
	}

	// 🔐 LOGIN: restituisce un token JWT
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		log.debug("Login attempt for username='{}'", request.getUsername());
		try {
			User user = authService.authenticate(request.getUsername(), request.getPassword());
			String token = jwtService.generateToken(user.getUsername(), user.getRole());
			log.info("User '{}' authenticated successfully", user.getUsername());
			return ResponseEntity.ok(new JwtResponse(token));
		} catch (RuntimeException e) {
			log.warn("Authentication failed for username='{}': {}", request.getUsername(), e.getMessage());
			return ResponseEntity.status(401).body("Credenziali non valide");
		}
	}

	// 📝 REGISTRAZIONE
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		log.debug("Registration attempt for username='{}'", request.getUsername());
		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			log.warn("Registration failed: user '{}' already exists", request.getUsername());
			return ResponseEntity.badRequest().body("Utente già esistente");
		}

		// ✅ Imposta protectedAdmin SOLO se il DB è vuoto
		boolean isFirstUser = userRepository.count() == 0;
		log.debug("Is first user? {}", isFirstUser);

		User user = User.builder().username(request.getUsername())
				.password(passwordEncoder.encode(request.getPassword())).role(request.getRole())
				.protectedAdmin(isFirstUser).build();

		userRepository.save(user);
		log.info("User '{}' registered successfully; protectedAdmin={}", user.getUsername(), isFirstUser);
		return ResponseEntity.ok("Registrazione completata");
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		// In un'app JWT pura, il logout è lato client.
		// Questo serve solo a livello simbolico, per logging o futura blacklist.
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7);
			// Puoi salvare in blacklist se vuoi invalidarlo a posteriori
			System.out.println("Logout token ricevuto: " + token);
		}
		return ResponseEntity.ok("Logout eseguito");
	}

	// 👥 Lista utenti (solo admin)
	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserDto>> getAllUsers() {
		log.debug("getAllUsers called");
		List<UserDto> users = userRepository.findAll().stream()
				.map(u -> new UserDto(u.getUsername(), u.getRole(), u.isProtectedAdmin())).toList();
		log.info("Retrieved {} users", users.size());
		return ResponseEntity.ok(users);
	}

	// ❌ Elimina utente (solo admin)
	@DeleteMapping("/{username}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable String username) {
		log.debug("deleteUser called for '{}'", username);
		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			log.warn("deleteUser: user '{}' not found", username);
			return ResponseEntity.notFound().build();
		}

		User user = optionalUser.get();
		if (user.isProtectedAdmin()) {
			log.warn("deleteUser forbidden: '{}' is protected admin", username);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Eliminazione dell'admin protetto non consentita");
		}

		userRepository.delete(user);
		log.info("User '{}' deleted", username);
		return ResponseEntity.ok("Utente eliminato: " + username);
	}

	@PutMapping("/update-role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateUserRole(@RequestBody Map<String, String> payload) {
		String username = payload.get("username");
		String role = payload.get("role");
		log.debug("updateUserRole called for '{}', newRole='{}'", username, role);

		Optional<User> optionalUser = userRepository.findByUsername(username);
		if (optionalUser.isEmpty()) {
			log.warn("updateUserRole: user '{}' not found", username);
			return ResponseEntity.notFound().build();
		}

		User user = optionalUser.get();

		if (user.isProtectedAdmin()) {
			log.warn("updateUserRole forbidden: '{}' is protected admin", username);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Modifica dell'admin protetto non consentita");
		}

		user.setRole(role);
		userRepository.save(user);
		log.info("User '{}' role updated to '{}'", username, role);
		return ResponseEntity.ok("Ruolo aggiornato");
	}

	@GetMapping("/is-empty")
	public ResponseEntity<Boolean> isUserTableEmpty() {
		log.debug("isUserTableEmpty called");
		return ResponseEntity.ok(userRepository.count() == 0);
	}
}
