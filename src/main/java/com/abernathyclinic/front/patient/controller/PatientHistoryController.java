package com.abernathyclinic.front.patient.controller;

import com.abernathyclinic.front.patient.model.PatientHistory;
import com.abernathyclinic.front.patient.service.AuthService;
import com.abernathyclinic.front.patient.service.PatientHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * Contrôleur pour gérer les opérations liées à l'historique des patients.
 */
@Controller
public class PatientHistoryController {

    private final PatientHistoryService patientHistoryService;
    private final AuthService authService;

    /**
     * Constructeur pour initialiser les services utilisés dans ce contrôleur.
     *
     * @param patientHistoryService le service pour gérer l'historique des patients
     * @param authService           le service pour gérer l'authentification et les tokens JWT
     */
    public PatientHistoryController(PatientHistoryService patientHistoryService, AuthService authService) {
        this.patientHistoryService = patientHistoryService;
        this.authService = authService;
    }

    /**
     * Récupère l'historique des notes pour un patient donné.
     *
     * @param id    l'identifiant du patient
     * @param model le modèle utilisé pour transmettre les données à la vue
     * @return le nom de la vue affichant l'historique du patient
     */
    @GetMapping("/patients/{id}/history")
    public String getPatientHistory(@PathVariable int id, Model model) {
        try {
            // Récupération des informations d'authentification
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Récupération du token JWT
            String jwtToken = authService.getOrRegenerateToken(authentication);

            // Appel au service pour récupérer l'historique du patient
            ResponseEntity<List<PatientHistory>> response = patientHistoryService.getPatientHistoryById(id, jwtToken);

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                model.addAttribute("history", response.getBody());
                model.addAttribute("patientId", id);
            } else {
                model.addAttribute("error", "Aucun historique trouvé pour ce patient.");
            }
        } catch (Exception e) {
            // Gestion des erreurs
            model.addAttribute("error", "Erreur lors de la récupération de l'historique : " + e.getMessage());
        }
        return "historyPatient";
    }

    /**
     * Ajoute une nouvelle note à l'historique d'un patient.
     *
     * @param id    l'identifiant du patient
     * @param note  l'objet contenant la nouvelle note à ajouter
     * @param model le modèle utilisé pour transmettre les données à la vue en cas d'erreur
     * @return une redirection vers la page de l'historique ou le nom de la vue en cas d'erreur
     */
    @PostMapping("/patients/{id}/history/add")
    public String addNoteToHistory(@PathVariable int id, @ModelAttribute PatientHistory note, Model model) {
        try {
            // Récupération des informations d'authentification
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Récupération du token JWT
            String jwtToken = authService.getOrRegenerateToken(authentication);

            // Création d'un nouvel objet PatientHistory avec la nouvelle note
            PatientHistory newNote = new PatientHistory();
            newNote.setPatId(String.valueOf(id));
            newNote.setNote(note.getNote());

            // Appel au service pour ajouter la nouvelle note
            this.patientHistoryService.addNoteToPatientHistory(id, newNote, jwtToken);

            // Redirection après succès
            return "redirect:/patients/" + id + "/history";
        } catch (Exception e) {
            // Gestion des erreurs
            model.addAttribute("error", "Erreur lors de l'ajout de la note : " + e.getMessage());
            return "historyPatient";
        }
    }
}
