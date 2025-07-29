package com.gianluca.security;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    //autowirwde effettuato ma non obbligatorio
	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public User authenticate(String username, String password) {
		System.out.println("Tentativo di login: " + username);
		return userRepository.findByUsername(username)
				.filter(user -> passwordEncoder.matches(password, user.getPassword()))
				.orElseThrow(() -> new RuntimeException("Credenziali non valide"));
	}

}
