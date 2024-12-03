package com.abernathyclinic.front.patient.repository;

import com.abernathyclinic.front.patient.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

}
