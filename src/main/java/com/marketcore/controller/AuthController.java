package com.marketcore.controller;

import com.marketcore.dto.AuthResponse;
import com.marketcore.dto.LoginRequest;
import com.marketcore.dto.RegisterRequest;
import com.marketcore.dto.UsuarioLogadoDTO;
import com.marketcore.entity.User;
import com.marketcore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Quem está logado. O front chama logo após o login para saber o nome e se é admin.
     * {@code @AuthenticationPrincipal} injeta o usuário que o JwtAuthenticationFilter autenticou.
     */
    @GetMapping("/me")
    public UsuarioLogadoDTO me(@AuthenticationPrincipal User usuario) {
        return authService.usuarioLogado(usuario);
    }
}
