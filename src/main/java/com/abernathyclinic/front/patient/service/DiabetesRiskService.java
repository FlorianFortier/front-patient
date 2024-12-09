package com.abernathyclinic.front.patient.service;

import com.abernathyclinic.front.patient.model.Patient;
import com.abernathyclinic.front.patient.model.PatientHistory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Service permettant de calculer le risque de diabète pour un patient.
 */
@Service
public class DiabetesRiskService {

    private final RestTemplate restTemplate;
    private static final String GATEWAY_DIABETES_URL = "http://192.168.0.102:9101/diabetes";
    private final PatientService patientService;

    /**
     * Constructeur pour injecter les dépendances nécessaires.
     *
     * @param restTemplate   Le client REST utilisé pour effectuer des requêtes HTTP.
     * @param patientService Le service utilisé pour récupérer les informations du patient.
     */
    public DiabetesRiskService(RestTemplate restTemplate, PatientService patientService) {
        this.restTemplate = restTemplate;
        this.patientService = patientService;
    }

    /**
     * Calcule l'âge d'une personne à partir de sa date de naissance.
     *
     * @param dateDeNaissance La date de naissance en tant que {@link LocalDate}.
     * @return L'âge en années.
     */
    public int calculerAge(LocalDate dateDeNaissance) {
        return Period.between(dateDeNaissance, LocalDate.now()).getYears();
    }

    /**
     * Calcule le risque de diabète pour un patient en fonction de son historique médical.
     *
     * @param history  La liste des antécédents médicaux du patient.
     * @param jwtToken Le token JWT pour l'authentification auprès de l'API Gateway.
     * @return Le niveau de risque calculé par le microservice Risk.
     */
    public String calculateRisk(List<PatientHistory> history, String jwtToken) {
        String url = GATEWAY_DIABETES_URL + "/risk";

        Patient retrievedPatient = patientService.getPatientById(history.get(0).getPatId(), jwtToken).getBody();

        Patient patient = new Patient();
        patient.setNom(history.get(0).getPatient());
        patient.setAge(calculerAge(retrievedPatient.getDateDeNaissance()));
        patient.setGenre(retrievedPatient.getGenre());
        patient.setNote(history.stream()
                .map(PatientHistory::getNote)
                .toList());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Patient> request = new HttpEntity<>(patient, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }
}

