package br.com.fiap.biblioteca.service;

import br.com.fiap.biblioteca.model.User;
import br.com.fiap.biblioteca.model.UserType;
import br.com.fiap.biblioteca.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegate = super.loadUser(userRequest);
        Map<String, Object> attrs = delegate.getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String oauthId;
        String email;
        String name;

        if ("github".equals(registrationId)) {
            oauthId = "github_" + String.valueOf(attrs.get("id"));
            email = (String) attrs.getOrDefault("email", null);
            name = (String) attrs.getOrDefault("name", (String) attrs.getOrDefault("login", "github_user"));
        } else if ("google".equals(registrationId)) {
            oauthId = "google_" + String.valueOf(attrs.get("sub"));
            email = (String) attrs.get("email");
            name = (String) attrs.get("name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        // Find existing user or create a new one
        userRepository.findByOauthId(oauthId).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setOauthId(oauthId);
            u.setRole(UserType.USER);
            u.setPassword("");
            return userRepository.save(u);
        });

        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String nameAttributeKey = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attrs, nameAttributeKey);
    }
}