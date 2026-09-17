package com.soufiane.userservice.service;

import com.soufiane.userservice.dto.AuthResponseDTO;
import com.soufiane.userservice.dto.LoginRequestDTO;
import com.soufiane.userservice.dto.RegisterRequestDTO;
import com.soufiane.userservice.exception.EmailAlreadyExistsException;
import com.soufiane.userservice.exception.InvalidCredentialsException;
import com.soufiane.userservice.model.Role;
import com.soufiane.userservice.model.User;
import com.soufiane.userservice.repository.UserRepository;
import com.soufiane.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + dto.getEmail());
        }
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.getEmail(), user.getRole().name());
    }
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.getEmail(), user.getRole().name());
    }
}
