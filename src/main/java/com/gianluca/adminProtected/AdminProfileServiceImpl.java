package com.gianluca.adminProtected;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gianluca.security.User;
import com.gianluca.security.UserRepository;
import com.gianluca.security.jwt.JwtService;

import java.util.Optional;

@Service
public class AdminProfileServiceImpl implements IAdminProfileService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> updateProfile(String jwt, UpdateProfileRequest request) {
        String username = jwtService.extractUsername(jwt);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty() || !optionalUser.get().isProtectedAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato");
        }

        User admin = optionalUser.get();
        admin.setUsername(request.getNewUsername());
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(admin);

        return ResponseEntity.ok("UPDATED_AND_LOGOUT");
    }
}
