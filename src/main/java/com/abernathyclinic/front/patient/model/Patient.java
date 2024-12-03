package com.abernathyclinic.front.patient.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Patient {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateDeNaissance;
    private String genre;
    private String adresse;
    private String telephone;
    private LocalDate lastModified;
    private LocalDate createdAt;
    private String whoLastModified;
    private Integer age;
    private List<String> note;

}
