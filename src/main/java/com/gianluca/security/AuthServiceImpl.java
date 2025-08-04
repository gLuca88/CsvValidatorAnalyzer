package com.gianluca.security;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    //autowirwde effettuato ma non obbligatorio
	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		log.debug("Initialized AuthServiceImpl with UserRepository={} and PasswordEncoder={}",
                userRepository.getClass().getSimpleName(),
                passwordEncoder.getClass().getSimpleName());
	}

	@Override
	public User authenticate(String username, String password) {
		log.debug("Tentativo di login per utente='{}'", username);
		System.out.println("Tentativo di login: " + username);
		return userRepository.findByUsername(username)
				.filter(user -> passwordEncoder.matches(password, user.getPassword()))
				.orElseThrow(() -> new RuntimeException("Credenziali non valide"));
	}

}
