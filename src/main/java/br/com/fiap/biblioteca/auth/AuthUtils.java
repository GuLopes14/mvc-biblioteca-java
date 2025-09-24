package br.com.fiap.biblioteca.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthUtils {

    // Set com todos os emails que têm permissão de admin
    private static Set<String> ADMIN_EMAILS = new HashSet<>();

    // Método que lê os emails de admin do application.properties
    @Value("${app.admin-emails}")
    public void setAdminEmails(String adminEmails) {
        if (adminEmails != null && !adminEmails.isEmpty()) {
            ADMIN_EMAILS = new HashSet<>(Arrays.asList(adminEmails.split(",")));
        }
    }

    // Método principal que verifica se um usuário é admin
    public static boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Verifica se é autenticação OAuth2 do Google
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            if ("google".equals(token.getAuthorizedClientRegistrationId())) {
                String email = extractEmail(authentication);
                return email != null && ADMIN_EMAILS.contains(email.trim());
            }
        }

        return false;
    }

    // Este método extrai o email do objeto de autenticação Google
    public static String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Google usa DefaultOidcUser
        if (principal instanceof DefaultOidcUser) {
            return ((DefaultOidcUser) principal).getEmail();
        }
        // Fallback para OAuth2User genérico (não deve ocorrer com nossa configuração)
        else if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute("email");
        }

        return null;
    }
}
