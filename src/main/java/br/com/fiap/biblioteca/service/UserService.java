package br.com.fiap.biblioteca.service;

import br.com.fiap.biblioteca.model.User;
import br.com.fiap.biblioteca.model.UserType;
import br.com.fiap.biblioteca.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService extends DefaultOAuth2UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final UserRepository userRepository;

    private Set<String> adminEmails = new HashSet<>();

    @Value("${app.admin-emails}")
    public void setAdminEmails(String adminEmailsStr) {
        if (adminEmailsStr != null && !adminEmailsStr.isEmpty()) {
            adminEmails = new HashSet<>(Arrays.asList(adminEmailsStr.split(",")));
        }
    }

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("UserService inicializado com repository: " + userRepository);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User register(OAuth2User oauth2User) {
        if (oauth2User == null) {
            logger.severe("Tentativa de registrar um usuário OAuth2 nulo");
            return null;
        }

        try {
            logger.info("Iniciando registro de usuário OAuth2: " + oauth2User.getName());

            // Verificar se temos um email (obrigatório)
            String email = oauth2User.getAttribute("email");
            if (email == null || email.trim().isEmpty()) {
                logger.severe("OAuth2User não possui email: " + oauth2User.getAttributes());
                return null;
            }

            return processGoogleUser(oauth2User);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao registrar usuário OAuth2: " + e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    private User processGoogleUser(OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String googleId = oauthUser.getAttribute("sub"); // Google usa "sub" como ID

        logger.info("Processando usuário Google - Email: " + email + ", Nome: " + name + ", ID: " + googleId);

        try {
            // Verifica se o usuário já existe pelo email
            var existingUser = userRepository.findByEmail(email);

            if (existingUser.isEmpty()) {
                // Cria um novo usuário
                logger.info("Criando novo usuário: " + email);
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setOauthId(googleId);
                newUser.setRole(isAdminEmail(email) ? UserType.ADMIN : UserType.USER);
                // Senha não é necessária para OAuth2, mas o campo não pode ser nulo
                newUser.setPassword("google-oauth2-" + System.currentTimeMillis());

                // Salvar explicitamente e verificar resultado
                User savedUser = userRepository.save(newUser);
                logger.info("Usuário criado com sucesso: ID=" + savedUser.getId());
                return savedUser;
            } else {
                // Atualiza usuário existente
                User user = existingUser.get();
                logger.info("Atualizando usuário existente: ID=" + user.getId() + ", Email=" + email);
                boolean updated = false;

                // Atualiza o nome se necessário
                if (name != null && !name.equals(user.getName())) {
                    user.setName(name);
                    updated = true;
                    logger.info("Nome atualizado para: " + name);
                }

                // Atualiza o ID OAuth se não estiver definido
                if (googleId != null && (user.getOauthId() == null || !googleId.equals(user.getOauthId()))) {
                    user.setOauthId(googleId);
                    updated = true;
                    logger.info("OAuth ID atualizado para: " + googleId);
                }

                // Atualiza o papel se necessário
                if (isAdminEmail(email) && user.getRole() != UserType.ADMIN) {
                    user.setRole(UserType.ADMIN);
                    updated = true;
                    logger.info("Papel atualizado para: ADMIN");
                } else if (!isAdminEmail(email) && user.getRole() == UserType.ADMIN) {
                    user.setRole(UserType.USER);
                    updated = true;
                    logger.info("Papel atualizado para: USER");
                }

                // Salva apenas se houver alterações
                if (updated) {
                    logger.info("Salvando atualizações para o usuário: " + user.getId());
                    return userRepository.save(user);
                }

                logger.info("Nenhuma alteração necessária para o usuário existente: " + user.getId());
                return user; // Retorna o usuário mesmo sem alterações
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao processar usuário Google: " + e.getMessage(), e);
            e.printStackTrace();
            throw e; // Re-throw para manter o comportamento transacional
        }
    }

    private boolean isAdminEmail(String email) {
        return email != null && adminEmails.contains(email.trim());
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("loadUser chamado para: " + userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            // Processa o usuário Google (registra/atualiza no banco)
            User user = processGoogleUser(oauth2User);
            logger.info("Usuário processado com sucesso no loadUser: " + (user != null ? user.getId() : "null"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao processar usuário no loadUser: " + e.getMessage(), e);
        }

        // Retorna o usuário OAuth2 original para o Spring Security
        return oauth2User;
    }
}
