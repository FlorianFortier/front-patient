package com.abernathyclinic.front.patient.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PatientHistory {

    private String id;
    private String patId;
    private String patient;
    private int age;
    private String genre;
    private String note;
}