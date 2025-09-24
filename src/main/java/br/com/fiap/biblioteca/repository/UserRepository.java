package br.com.fiap.biblioteca.repository;

import br.com.fiap.biblioteca.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthId(String oauthId);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    
    default Optional<User> findByUsername(String username) {
        return findByName(username);
    }
    
    default Optional<User> findByGithubId(String githubId) {
        return findByOauthId(githubId);
    }
    
    default boolean existsByUsername(String username) {
        return existsByName(username);
    }
}