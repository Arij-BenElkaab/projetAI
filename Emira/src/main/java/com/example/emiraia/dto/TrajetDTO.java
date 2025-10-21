package com.example.emiraia.dto;

import com.example.emiraia.entities.Trajet;
import java.time.Duration;
import java.time.LocalDateTime;

public class TrajetDTO {
    private Trajet trajet;
    private long minutesBeforeDeadline; // How many minutes before the requested latest arrival
    private String note; // Optional info for frontend

    public TrajetDTO(Trajet trajet, LocalDateTime latestArriveeTime) {
        this.trajet = trajet;
        if (latestArriveeTime != null) {
            this.minutesBeforeDeadline = Duration.between(trajet.getHeureDepart(), latestArriveeTime).toMinutes();
            this.note = "Arrives " + minutesBeforeDeadline + " minutes before requested time";
        } else {
            this.minutesBeforeDeadline = -1;
            this.note = "";
        }
    }

    // getters
    public Trajet getTrajet() { return trajet; }
    public long getMinutesBeforeDeadline() { return minutesBeforeDeadline; }
    public String getNote() { return note; }

    public double getPrixFinal() {
        return trajet.getPrixBase(); // You can adjust if you want to multiply by requested seats
    }
}

