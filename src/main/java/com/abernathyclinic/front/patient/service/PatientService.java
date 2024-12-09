package com.abernathyclinic.front.patient.service;

import com.abernathyclinic.front.patient.model.Patient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service pour gérer les opérations relatives aux patients via la Gateway.
 */
@Service
public class PatientService {

    private static final String GATEWAY_PATIENT_URL = "http://192.168.0.102:9101/api/patients";
    private final RestTemplate restTemplate;

    /**
     * Constructeur pour injecter le client REST.
     *
     * @param restTemplate Le client REST utilisé pour effectuer des requêtes HTTP.
     */
    public PatientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère la liste de tous les patients.
     *
     * @param jwtToken Le token JWT utilisé pour l'authentification.
     * @return Une réponse contenant un tableau d'objets représentant les patients.
     * @throws RuntimeException Si le token JWT est expiré ou invalide.
     */
    public ResponseEntity<Object[]> getAllPatients(String jwtToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            return restTemplate.exchange(GATEWAY_PATIENT_URL, HttpMethod.GET, request, Object[].class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("Le token JWT a expiré ou est invalide");
        }
    }

    /**
     * Récupère un patient à partir de son identifiant.
     *
     * @param id    L'identifiant du patient.
     * @param token Le token JWT utilisé pour l'authentification.
     * @return Une réponse contenant les détails du patient.
     * @throws RuntimeException En cas d'erreur lors de la récupération du patient.
     */
    public ResponseEntity<Patient> getPatientById(String id, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = GATEWAY_PATIENT_URL + "/" + id;

            return restTemplate.exchange(url, HttpMethod.GET, entity, Patient.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Erreur lors de la récupération du patient : " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau patient.
     *
     * @param patient  L'objet représentant le patient à créer.
     * @param jwtToken Le token JWT utilisé pour l'authentification.
     */
    public void createPatient(Patient patient, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Patient> request = new HttpEntity<>(patient, headers);
        restTemplate.postForEntity(GATEWAY_PATIENT_URL, request, Patient.class);
    }

    /**
     * Met à jour les informations d'un patient existant.
     *
     * @param patient  L'objet représentant le patient avec les nouvelles informations.
     * @param jwtToken Le token JWT utilisé pour l'authentification.
     * @throws IllegalArgumentException Si l'ID du patient est nul.
     */
    public void updatePatient(Patient patient, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Patient> request = new HttpEntity<>(patient, headers);
        String url = GATEWAY_PATIENT_URL + "/" + patient.getId();

        if (patient.getId() == null) {
            throw new IllegalArgumentException("L'ID du patient ne peut pas être nul");
        }

        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
    }
}

