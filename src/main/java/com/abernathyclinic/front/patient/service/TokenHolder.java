package com.abernathyclinic.front.patient.service;

public class TokenHolder {

    private static String jwtToken;

    /**
     * Static
     * @return  Récupère le token jwt
     */
    public static String getJwtToken() {
        return jwtToken;
    }

    /**
     * Stocke le token jwt
     * @param token
     */
    public static void setJwtToken(String token) {
        jwtToken = token;
    }
}
