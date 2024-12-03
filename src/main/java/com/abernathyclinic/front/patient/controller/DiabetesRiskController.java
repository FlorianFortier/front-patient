package com.abernathyclinic.front.patient.controller;

import com.abernathyclinic.front.patient.model.PatientHistory;
import com.abernathyclinic.front.patient.service.AuthService;
import com.abernathyclinic.front.patient.service.DiabetesRiskService;
import com.abernathyclinic.front.patient.service.PatientHistoryService;
import com.abernathyclinic.front.patient.service.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour gérer les opérations liées au calcul du risque de diabète pour un patient.
 */
@Controller
public class DiabetesRiskController {

    private final DiabetesRiskService diabetesRiskService;
    private final AuthService authService;
    private final PatientHistoryService patientHistoryService;

    /**
     * Constructeur pour initialiser les services utilisés dans ce contrôleur.
     *
     * @param diabetesRiskService  le service pour calculer le risque de diabète
     * @param authService          le service d'authentification pour gérer les tokens JWT
     * @param patientService       le service pour gérer les données des patients (non utilisé ici)
     * @param patientHistoryService le service pour récupérer l'historique médical des patients
     */
    public DiabetesRiskController(DiabetesRiskService diabetesRiskService, AuthService authService, PatientService patientService, PatientHistoryService patientHistoryService) {
        this.diabetesRiskService = diabetesRiskService;
        this.authService = authService;
        this.patientHistoryService = patientHistoryService;
    }

    /**
     * Endpoint pour calculer le risque de diabète pour un patient spécifique.
     *
     * @param id                 l'identifiant du patient dont le risque doit être calculé
     * @param redirectAttributes attributs pour transmettre des messages de succès ou d'erreur à la redirection
     * @return une redirection vers la page de l'historique du patient après le calcul du risque
     */
    @PostMapping("/diabetes/calculate-risk")
    public String calculateRisk(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            // Récupération du token JWT pour l'utilisateur actuel
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jwtToken = authService.getOrRegenerateToken(authentication);

            // Récupération de l'historique médical du patient
            List<PatientHistory> patientHistoryList = patientHistoryService.getPatientHistoryById(Integer.parseInt(id), jwtToken).getBody();
            List<String> patientNoteList = new ArrayList<>();
            patientHistoryList.forEach(patientHistory -> {
                patientNoteList.add(patientHistory.getNote());
            });

            // Calcul du risque basé sur l'historique médical
            String riskResult = diabetesRiskService.calculateRisk(patientHistoryList, jwtToken);

            // Ajout d'un message de succès dans les attributs de redirection
            redirectAttributes.addFlashAttribute("success", "Risque calculé avec succès : " + riskResult);
        } catch (Exception e) {
            // Gestion des erreurs et ajout d'un message d'erreur dans les attributs de redirection
            redirectAttributes.addFlashAttribute("error", "Erreur lors du calcul du risque : " + e.getMessage());
        }

        // Redirection vers la page de l'historique du patient
        return "redirect:/patients/" + id + "/history";
    }
}

