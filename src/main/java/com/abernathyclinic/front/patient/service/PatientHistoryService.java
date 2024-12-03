package com.abernathyclinic.front.patient.service;

import com.abernathyclinic.front.patient.model.PatientHistory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service pour gérer l'historique médical des patients.
 */
@Service
public class PatientHistoryService {

    private final RestTemplate restTemplate;

    /**
     * Constructeur pour injecter le client REST.
     *
     * @param restTemplate Le client REST utilisé pour effectuer des requêtes HTTP.
     */
    public PatientHistoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère l'historique médical d'un patient à partir de son identifiant.
     *
     * @param patientId L'identifiant du patient.
     * @param jwtToken  Le token JWT utilisé pour l'authentification.
     * @return Une réponse contenant la liste des historiques du patient.
     */
    public ResponseEntity<List<PatientHistory>> getPatientHistoryById(int patientId, String jwtToken) {
        String url = "http://192.168.0.102:9101/api/gateway/history/" + patientId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<PatientHistory>>() {
                }
        );
    }

    /**
     * Ajoute une note à l'historique médical d'un patient.
     *
     * @param patientId L'identifiant du patient.
     * @param note      La note à ajouter à l'historique.
     * @param jwtToken  Le token JWT utilisé pour l'authentification.
     */
    public void addNoteToPatientHistory(int patientId, PatientHistory note, String jwtToken) {
        String url = "http://192.168.0.102:9101/api/gateway/history/" + patientId + "/add";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PatientHistory> request = new HttpEntity<>(note, headers);

        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }
}
