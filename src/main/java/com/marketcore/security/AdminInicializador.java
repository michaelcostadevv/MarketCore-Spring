package com.marketcore.security;

import com.marketcore.entity.User;
import com.marketcore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Cria o primeiro administrador na inicialização, a partir das variáveis ADMIN_EMAIL e ADMIN_PASSWORD.
 * Se elas não estiverem definidas, ou se o usuário já existir, não faz nada.
 */
@Component
public class AdminInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdminInicializador(UserRepository userRepository, PasswordEncoder passwordEncoder,
                              @Value("${app.admin.email:}") String email,
                              @Value("${app.admin.password:}") String senha) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank() || userRepository.findByEmail(email).isPresent()) {
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(senha));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        log.info("Administrador inicial criado: {}", email);
    }
}
