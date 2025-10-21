package com.example.emiraia.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trajets")
public class Trajet {
    @Id
    private String id;
    private String conducteurId;
    private String depart;
    private String arrivee;
    private LocalDateTime heureDepart;
    private int placesDisponibles;
    private double prixBase;



    public Trajet() {}

    public Trajet(String conducteurId, String depart, String arrivee, LocalDateTime heureDepart, int places, double prixBase) {
        this.conducteurId = conducteurId;
        this.depart = depart;
        this.arrivee = arrivee;
        this.heureDepart = heureDepart;
        this.placesDisponibles = places;
        this.prixBase = prixBase;


    }

    // getters et setters
    public String getId() { return id; }
    public String getConducteurId() { return conducteurId; }
    public void setConducteurId(String conducteurId) { this.conducteurId = conducteurId; }
    public String getDepart() { return depart; }
    public void setDepart(String depart) { this.depart = depart; }
    public String getArrivee() { return arrivee; }
    public void setArrivee(String arrivee) { this.arrivee = arrivee; }
    public LocalDateTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalDateTime heureDepart) { this.heureDepart = heureDepart; }
    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }
    public double getPrixBase() { return prixBase; }
    public void setPrixBase(double prixBase) { this.prixBase = prixBase; }
}