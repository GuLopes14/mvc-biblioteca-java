package br.com.fiap.biblioteca.model;

import jakarta.persistence.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserType role = UserType.USER;
    
    @Column(name = "oauth_id")
    private String oauthId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Loan> loans;
    
    public User() {}
    
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    
    /**
     * Construtor para criar um usuário a partir de um OAuth2User
     * @param oauth2User O usuário OAuth2 autenticado
     */
    public User(OAuth2User oauth2User) {
        this.email = oauth2User.getAttribute("email");

        // O nome pode estar em atributos diferentes dependendo do provedor
        String name = oauth2User.getAttribute("name");
        if (name == null) {
            name = oauth2User.getAttribute("login"); // GitHub usa login
        }
        if (name == null) {
            name = this.email.split("@")[0]; // Fallback para parte do email
        }
        this.name = name;

        // Para o ID OAuth, verifica diferentes atributos
        String oauthId = null;
        if (oauth2User.getAttribute("id") != null) {
            oauthId = oauth2User.getAttribute("id").toString(); // GitHub
        } else if (oauth2User.getAttribute("sub") != null) {
            oauthId = oauth2User.getAttribute("sub"); // Google
        }
        this.oauthId = oauthId;

        // Senha aleatória - nunca será usada para login
        this.password = UUID.randomUUID().toString();

        // Por padrão é USER, será modificado pelo UserService se necessário
        this.role = UserType.USER;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public UserType getRole() { return role; }
    public void setRole(UserType role) { this.role = role; }
    
    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }
    
    public String getUsername() { return name; }
    public void setUsername(String username) { this.name = username; }
    
    public UserType getType() { return role; }
    public void setType(UserType type) { this.role = type; }
    
    public String getGithubId() { return oauthId; }
    public void setGithubId(String githubId) { this.oauthId = githubId; }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}