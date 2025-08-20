package com.stockmarketproject.controller;
import com.stockmarketproject.dto.RegisterRequest;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(req.email(), req.password()));
    }


    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication auth) {
        User u = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(u);
    }
}
