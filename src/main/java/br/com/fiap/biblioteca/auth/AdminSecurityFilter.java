package br.com.fiap.biblioteca.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// Filtro que será executado uma vez por requisição
public class AdminSecurityFilter extends OncePerRequestFilter {
    // Lista de rotas que exigem permissão de administrador
    private static final List<String> ADMIN_ROUTES = Arrays.asList(
            "/books/add",
            "/books/create",
            "/books/edit",
            "/books/delete",
            "/users/create",
            "/users/edit",
            "/users/delete"
    );

    // Método principal do filtro, chamado a cada requisição
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Obtém a URI da requisição atual
        String requestURI = request.getRequestURI();

        // Verifica se a URI começa com alguma das rotas administrativas
        boolean isAdminRoute = ADMIN_ROUTES.stream()
                .anyMatch(requestURI::startsWith);

        // Se for rota de admin, verifica se o usuário é administrador
        if (isAdminRoute) {
            // Recupera a autenticação do usuário logado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Verifica se é uma autenticação OAuth2 do Google e se é admin
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                if ("google".equals(token.getAuthorizedClientRegistrationId())) {
                    // Usa AuthUtils para verificar se o usuário é admin
                    boolean isAdmin = AuthUtils.isAdmin(authentication);

                    // Se não for admin, retorna erro 403 (proibido)
                    if (!isAdmin) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Apenas administradores podem acessar esta página.");
                        return;
                    }
                } else {
                    // Não é autenticação Google
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Autenticação Google é necessária.");
                    return;
                }
            } else {
                // Não é autenticação OAuth2
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Autenticação Google é necessária.");
                return;
            }
        }

        // Se não for rota admin ou usuário for admin, segue o fluxo normalmente
        filterChain.doFilter(request, response);
    }
}
