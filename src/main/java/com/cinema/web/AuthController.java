package com.cinema.web;

import com.cinema.domain.entity.AppUser;
import com.cinema.domain.repository.AppUserRepository;
import com.cinema.web.dto.AuthResponse;
import com.cinema.web.dto.LoginRequest;
import com.cinema.web.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AppUserRepository appUserRepo;

    public AuthController(AppUserRepository appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        if (appUserRepo.findByUsername(req.username()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Username already exists", null, null, null));
        }

        AppUser newUser = new AppUser(req.username(), req.password(), "USER");
        appUserRepo.save(newUser);

        return ResponseEntity.ok(new AuthResponse(true, "Registration successful", newUser.getId().toString(), newUser.getUsername(), newUser.getRole()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        AppUser user = appUserRepo.findByUsername(req.username());
        if (user != null && user.getPassword().equals(req.password())) {
            return ResponseEntity.ok(new AuthResponse(true, "Login successful", user.getId().toString(), user.getUsername(), user.getRole()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid credentials", null, null, null));
        }
    }
}
