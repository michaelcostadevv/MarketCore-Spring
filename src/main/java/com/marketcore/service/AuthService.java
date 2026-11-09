package com.marketcore.service;

import com.marketcore.dto.AuthResponse;
import com.marketcore.dto.LoginRequest;
import com.marketcore.dto.RegisterRequest;
import com.marketcore.entity.User;
import com.marketcore.exception.ConflitoException;
import com.marketcore.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    /** Todo cadastro público recebe o papel mais baixo. Administradores são criados pelo AdminInicializador. */
    public static final String ROLE_PADRAO = "ROLE_USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizar(request.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(ROLE_PADRAO);
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        // Lança BadCredentialsException (-> 401) se e-mail ou senha estiverem errados
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizar(request.getEmail()), request.getPassword()));

        return new AuthResponse(jwtService.generateToken((UserDetails) auth.getPrincipal()));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
