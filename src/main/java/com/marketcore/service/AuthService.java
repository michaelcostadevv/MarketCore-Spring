package com.marketcore.service;

import com.marketcore.dto.AuthResponse;
import com.marketcore.dto.LoginRequest;
import com.marketcore.dto.RegisterRequest;
import com.marketcore.dto.UsuarioLogadoDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.entity.User;
import com.marketcore.exception.ConflitoException;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    /** Todo cadastro público recebe o papel mais baixo. Administradores são criados pelo AdminInicializador. */
    public static final String ROLE_PADRAO = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, ClienteRepository clienteRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Cria a conta de login E o cadastro de cliente.
     * {@code @Transactional} torna as duas gravações atômicas: se salvar o cliente falhar,
     * o usuário também é desfeito (rollback) e não sobra uma conta "órfã" que loga mas não compra.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizar(request.getEmail());
        if (userRepository.findByEmail(email).isPresent() || clienteRepository.existsByEmail(email)) {
            throw new ConflitoException("Já existe um cadastro com este e-mail");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(ROLE_PADRAO);
        userRepository.save(user);

        clienteRepository.save(new Cliente(request.getNome().trim(), email, user));

        return new AuthResponse(jwtService.generateToken(user));
    }

    /** Dados do usuário logado para o frontend (GET /auth/me). */
    @Transactional(readOnly = true)
    public UsuarioLogadoDTO usuarioLogado(User usuario) {
        Optional<Cliente> cliente = clienteRepository.findByUsuarioId(usuario.getId());
        return new UsuarioLogadoDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                ROLE_ADMIN.equals(usuario.getRole()),
                cliente.map(Cliente::getId).orElse(null),
                cliente.map(Cliente::getNome).orElse(null));
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
