package com.gianluca.adminProtected;


import org.springframework.http.ResponseEntity;

public interface IAdminProfileService {
    ResponseEntity<String> updateProfile(String jwt, UpdateProfileRequest request);
}