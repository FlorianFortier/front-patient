package com.abernathyclinic.front.patient.controller;

import com.abernathyclinic.front.patient.model.Patient;
import com.abernathyclinic.front.patient.service.AuthService;
import com.abernathyclinic.front.patient.service.PatientHistoryService;
import com.abernathyclinic.front.patient.service.PatientService;
import com.abernathyclinic.front.patient.service.TokenHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

/**
 * Contrôleur pour la gestion des détails des patients, leur création et leurs mises à jour.
 */
@Controller
public class DetailPatientController {

    private final PatientService patientService;
    private final AuthService authService;

    /**
     * Constructeur pour le contrôleur DetailPatientController.
     *
     * @param patientService  le service pour les opérations sur les patients
     * @param authService     le service pour les opérations d'authentification
     */
    public DetailPatientController(PatientService patientService, AuthService authService, PatientHistoryService patientHistoryService) {
        this.patientService = patientService;
        this.authService = authService;
    }

    /**
     * Récupère les détails d'un patient spécifique via son ID.
     *
     * @param id    l'identifiant du patient
     * @param model le modèle à remplir avec les détails du patient
     * @return le nom de la vue pour afficher les détails du patient
     */
    @GetMapping("/patients/{id}")
    public String getPatientDetails(@PathVariable String id, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jwtToken = authService.getOrRegenerateToken(authentication);

            ResponseEntity<Patient> response = patientService.getPatientById(id, jwtToken);
            Patient patient = response.getBody();

            if (patient != null) {
                model.addAttribute("patient", patient);
                return "detailUtilisateur";
            } else {
                model.addAttribute("error", "Aucun détail trouvé pour ce patient.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
        }
        return "detailUtilisateur";
    }

    /**
     * Affiche le formulaire de création d'un nouveau patient.
     *
     * @param model le modèle à remplir avec un patient vide
     * @return le nom de la vue partagée pour la création et la modification d'un patient
     */
    @GetMapping("/patients")
    public String createPatientForm(Model model) {
        Patient patient = new Patient();
        patient.setCreatedAt(LocalDate.now());

        model.addAttribute("patient", patient); // Nouveau patient vide
        return "detailUtilisateur"; // Vue partagée pour création et modification
    }

    /**
     * Crée un nouveau patient.
     *
     * @param patient le patient à créer
     * @param model   le modèle pour afficher des messages ou des erreurs
     * @return une redirection vers la page d'accueil après succès ou la vue de création en cas d'erreur
     */
    @PostMapping("/patients")
    public String createPatient(@ModelAttribute Patient patient, Model model) {
        try {
            String jwtToken = TokenHolder.getJwtToken();
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            patient.setWhoLastModified(currentUser); // Utilisateur responsable de la création
            patientService.createPatient(patient, jwtToken); // Appel du service pour créer un patient
            model.addAttribute("message", "Le patient a été créé avec succès.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "detailUtilisateur";
        }
        return "redirect:/home";
    }

    /**
     * Met à jour les informations d'un patient existant.
     *
     * @param patient le patient avec les informations mises à jour
     * @param model   le modèle pour afficher des messages ou des erreurs
     * @return une redirection vers la page de détails du patient après succès ou la vue de modification en cas d'erreur
     */
    @PostMapping("/patients/edit")
    public String editPatient(@ModelAttribute Patient patient, Model model) {
        try {
            String jwtToken = TokenHolder.getJwtToken();
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            patient.setWhoLastModified(currentUser);
            patientService.updatePatient(patient, jwtToken);
            model.addAttribute("message", "Les modifications ont été enregistrées avec succès.");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "detailUtilisateur";
        }
        return "redirect:/patients/" + patient.getId();
    }
}

