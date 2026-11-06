package com.inventory.auth.service;

import com.inventory.auth.dto.LoginRequest;
import com.inventory.auth.dto.RegisterRequest;
import com.inventory.auth.model.User;
import com.inventory.auth.repository.RoleRepository;
import com.inventory.auth.repository.UserRepository;
import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Map<String, Object> login(LoginRequest req) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user, Map.of("role", user.getRoleName(), "uid", user.getId()));
        return Map.of("token", token, "user", user);
    }

    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) throw new ConflictException("Username already exists");
        if (userRepository.existsByEmail(req.getEmail())) throw new ConflictException("Email already exists");
        var role = roleRepository.findByName(req.getRole() == null ? "Analyst" : req.getRole())
                .orElseThrow(() -> new BadRequestException("Unknown role: " + req.getRole()));
        User u = User.builder()
                .username(req.getUsername()).email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roleId(role.getId()).roleName(role.getName()).active(true).build();
        return userRepository.save(u);
    }
}
