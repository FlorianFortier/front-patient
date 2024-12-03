package com.abernathyclinic.front.patient.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


/**
 * Configuration de la sécurité pour l'application Spring Boot.
 * Gère l'authentification, l'autorisation et la configuration des filtres de sécurité.
 */
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    /**
     * Constructeur pour injecter le service de gestion des utilisateurs.
     *
     * @param userDetailsService le service utilisé pour charger les informations des utilisateurs.
     */
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configure la chaîne de filtres de sécurité Spring Security.
     *
     * @param http l'objet {@link HttpSecurity} utilisé pour configurer les filtres.
     * @return une instance de {@link SecurityFilterChain} configurée.
     * @throws Exception si une erreur survient lors de la configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // Active les règles de CORS par défaut.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/login/**", // Permet l'accès sans authentification aux pages de connexion.
                                "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error/**"
                        ).permitAll() // Autorise l'accès aux ressources statiques et aux erreurs.
                        .anyRequest().authenticated() // Requiert une authentification pour toutes les autres requêtes.
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/home", true) // Redirige vers "/home" après une connexion réussie.
                        .permitAll()); // Permet l'accès à la page de connexion.

        return http.build();
    }

    /**
     * Crée un bean {@link PasswordEncoder} utilisant BCrypt pour l'encodage des mots de passe.
     *
     * @return une instance de {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // Vérifie si le mot de passe est soit en clair, soit encodé avec BCrypt.
                return rawPassword.toString().equals(encodedPassword) || bcrypt.matches(rawPassword, encodedPassword);
            }
        };
    }

    /**
     * Crée un bean {@link DaoAuthenticationProvider} pour la gestion de l'authentification.
     *
     * @return une instance de {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Crée un bean {@link AuthenticationManager} pour gérer l'authentification.
     *
     * @param http l'objet {@link HttpSecurity} utilisé pour configurer l'authentification.
     * @return une instance de {@link AuthenticationManager}.
     * @throws Exception si une erreur survient lors de la configuration.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authProvider())
                .build();
    }
}
