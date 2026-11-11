package com.marketcore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Conta de login dona deste cliente (relação 1:1).
     * - A chave estrangeira fica aqui: o cliente pertence a um usuário; User não precisa conhecer Cliente.
     * - unique = true garante o 1:1 no próprio banco, não só no código.
     * - nullable: clientes cadastrados antes desta versão não têm usuário.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private User usuario;

    public Cliente() {}

    public Cliente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public Cliente(String nome, String email, User usuario) {
        this(nome, email);
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User getUsuario() { return usuario; }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nome=" + nome + ", email=" + email + '}';
    }
}
