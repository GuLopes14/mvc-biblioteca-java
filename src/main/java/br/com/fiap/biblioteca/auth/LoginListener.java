package br.com.fiap.biblioteca.auth;


import br.com.fiap.biblioteca.service.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class LoginListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private static final Logger logger = Logger.getLogger(LoginListener.class.getName());
    private final UserService userService;

    public LoginListener(UserService userService) {
        this.userService = userService;
        logger.info("LoginListener inicializado");
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        logger.info("Evento de autenticação recebido: " + event);

        try {
            // Verifica se é uma autenticação OAuth2
            if (event.getAuthentication() instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) event.getAuthentication();
                logger.info("Autenticação OAuth2 detectada. Provedor: " + token.getAuthorizedClientRegistrationId());

                // Verifica se é do provedor Google
                if ("google".equals(token.getAuthorizedClientRegistrationId())) {
                    // Processa o usuário Google
                    logger.info("Login Google detectado!");
                    OAuth2User oauth2User = (OAuth2User) event.getAuthentication().getPrincipal();
                    logger.info("Atributos do usuário: " + oauth2User.getAttributes());

                    // Chama o método register e armazena o resultado
                    var user = userService.register(oauth2User);
                    logger.info("Usuário processado: " + (user != null ? "ID=" + user.getId() + ", Email=" + user.getEmail() : "NULL"));
                }
            } else {
                logger.warning("Autenticação não é do tipo OAuth2: " + event.getAuthentication().getClass().getName());
            }
        } catch (Exception e) {
            logger.severe("ERRO ao processar evento de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
