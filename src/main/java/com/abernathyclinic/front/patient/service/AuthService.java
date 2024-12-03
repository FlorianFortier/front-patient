package com.abernathyclinic.front.patient.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion de l'authentification et des tokens JWT.
 */
@Service
public class AuthService {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    private static final String AUTH_SERVICE_URL = "http://192.168.0.102:9101/api/auth/token";
    private final RestTemplate restTemplate;

    /**
     * Constructeur pour injecter le service {@link RestTemplate}.
     *
     * @param restTemplate le client REST utilisé pour les appels HTTP.
     */
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Génère un token JWT basé sur le contexte d'authentification.
     *
     * @param authentication le contexte d'authentification.
     * @return le token JWT généré.
     */
    public String generateTokenFromContext(Authentication authentication) {
        // Créer une charge utile avec les informations utilisateur
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", authentication.getName());
        claims.put("roles", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Préparer et envoyer la requête pour générer le token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(claims, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(AUTH_SERVICE_URL, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody(); // Retourner le JWT
        } else {
            throw new RuntimeException("Échec de la génération du token JWT : " + response.getStatusCode());
        }
    }

    /**
     * Récupère la clé secrète pour signer ou valider le token.
     *
     * @return une instance de {@link Key}.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Vérifie si un token JWT est valide.
     *
     * @param token le token JWT à valider.
     * @return true si le token est valide, false sinon.
     */
    public boolean isTokenValid(String token) {
        try {
            // Extraire les claims et vérifier l'expiration
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Token invalide ou erreur de validation
        }
    }

    /**
     * Extrait toutes les claims contenues dans un token JWT.
     *
     * @param token le token JWT.
     * @return les claims extraites.
     */
    private Claims extractAllClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Le token ne peut pas être null ou vide");
        }
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .setAllowedClockSkewSeconds(60) // Permet une tolérance de décalage horaire
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Récupère un token existant ou génère un nouveau token JWT.
     *
     * @param authentication le contexte d'authentification de l'utilisateur.
     * @return le token JWT valide.
     * @throws RuntimeException si l'utilisateur n'est pas authentifié ou si une erreur survient.
     */
    public String getOrRegenerateToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = null;

            if (principal instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            }

            if (username == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }

            // Vérification ou régénération du token
            String jwtToken = TokenHolder.getJwtToken();
            if (jwtToken == null || jwtToken.isEmpty() || !isTokenValid(jwtToken)) {
                jwtToken = generateTokenFromContext(authentication);
                TokenHolder.setJwtToken(jwtToken);
            }
            return jwtToken;
        }
        throw new RuntimeException("Authentication est null ou non authentifiée");
    }
}
