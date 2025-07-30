package com.gianluca.adminProtected;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gianluca.security.User;
import com.gianluca.security.UserRepository;
import com.gianluca.security.jwt.JwtService;

@RestController
@RequestMapping("/api/admin-profile")
public class AdminProfileController {

	@Autowired
	private IAdminProfileService profileService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@PutMapping("/update")
	public ResponseEntity<String> update(@RequestBody UpdateProfileRequest request,
			@RequestHeader("Authorization") String token) {
		String jwt = token.replace("Bearer ", "");
		return profileService.updateProfile(jwt, request);
	}

	@GetMapping("/is-protected-admin")
	public ResponseEntity<Boolean> isProtectedAdmin(@RequestHeader("Authorization") String token) {
		String jwt = token.replace("Bearer ", "");
		String username = jwtService.extractUsername(jwt);
		Optional<User> user = userRepository.findByUsername(username);
		return ResponseEntity.ok(user.map(User::isProtectedAdmin).orElse(false));
	}
}
