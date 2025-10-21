package com.example.emiraia.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reservations")
public class Reservation {
    @Id
    private String id;
    private String trajetId;
    private String passagerId;
    private int places;
    private double prixFinal;

    public Reservation() {}

    public Reservation(String trajetId, String passagerId, int places, double prixFinal) {
        this.trajetId = trajetId;
        this.passagerId = passagerId;
        this.places = places;
        this.prixFinal = prixFinal;
    }

    // getters et setters
    public String getId() { return id; }
    public String getTrajetId() { return trajetId; }
    public void setTrajetId(String trajetId) { this.trajetId = trajetId; }
    public String getPassagerId() { return passagerId; }
    public void setPassagerId(String passagerId) { this.passagerId = passagerId; }
    public int getPlaces() { return places; }
    public void setPlaces(int places) { this.places = places; }
    public double getPrixFinal() { return prixFinal; }
    public void setPrixFinal(double prixFinal) { this.prixFinal = prixFinal; }
}