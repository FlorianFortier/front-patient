package com.abernathyclinic.front.patient.controller;

import com.abernathyclinic.front.patient.service.AuthService;
import com.abernathyclinic.front.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur pour gérer les opérations liées à la page d'accueil.
 */
@Controller
public class HomeController {

    private final AuthService authService;
    private final PatientService patientService;

    /**
     * Constructeur pour initialiser les services utilisés dans ce contrôleur.
     *
     * @param authService    le service d'authentification pour gérer les tokens JWT
     * @param patientService le service pour gérer les données des patients
     */
    public HomeController(AuthService authService, PatientService patientService) {
        this.authService = authService;
        this.patientService = patientService;
    }

    /**
     * Gère l'affichage de la page d'accueil.
     *
     * @param model le modèle utilisé pour transmettre des données à la vue
     * @return le nom de la vue correspondant à la page d'accueil
     */
    @GetMapping("/home")
    public String home(Model model) {
        try {
            // Étape 1 : Récupération des informations d'authentification
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Étape 2 : Récupération du token JWT
            String jwtToken = authService.getOrRegenerateToken(authentication);

            // Étape 3 : Récupération de la liste des patients via le service
            ResponseEntity<Object[]> response = patientService.getAllPatients(jwtToken);
            List<Object> patients = Arrays.asList(response.getBody());
            model.addAttribute("patients", patients);

            return "home"; // Vue de la page d'accueil
        } catch (Exception e) {
            // Gestion des erreurs
            model.addAttribute("error", "Erreur : " + e.getMessage());
        }
        return "home";
    }
}
