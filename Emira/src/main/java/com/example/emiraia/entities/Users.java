package com.example.emiraia.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "users")
public class Users {


    @Id
    private String id;
    private String nom;
    private String role; // "passager" ou "conducteur"

    public void User() {}

    public void User(String nom, String role) {
        this.nom = nom;
        this.role = role;
    }

    // getters et setters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

}
