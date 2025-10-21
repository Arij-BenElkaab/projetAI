package com.example.emiraia.controllers;

import com.example.emiraia.dto.TrajetDTO;
import com.example.emiraia.entities.Reservation;
import com.example.emiraia.entities.Trajet;
import com.example.emiraia.services.TrajetService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trajets")
@CrossOrigin(origins = "http://localhost:4200")
public class TrajetController {

    private final TrajetService trajetService;

    public TrajetController(TrajetService trajetService) {
        this.trajetService = trajetService;
    }

    @PostMapping("/search")
    public List<TrajetDTO> searchTrajets(@RequestBody Map<String, Object> request) {
        String depart = (String) request.get("depart");
        String arrivee = (String) request.get("arrivee");
        String latestArriveeStr = (String) request.get("latestArriveeTime");
        LocalDateTime latestArriveeTime = latestArriveeStr != null ? LocalDateTime.parse(latestArriveeStr) : null;
        int places = (int) request.getOrDefault("places", 1);

        return trajetService.searchTrajets(depart, arrivee, latestArriveeTime, places);
    }

        @PostMapping("/{trajetId}/reserver")
        public Reservation reserver(@PathVariable String trajetId, @RequestBody Map<String, Object> request) {
            String passengerId = (String) request.get("passagerId");
            int places = (int) request.getOrDefault("places", 1);
            return trajetService.reserverTrajet(trajetId, passengerId, places);
        }

    @PostMapping("/add")
    public ResponseEntity<Trajet> addTrajet(@RequestBody Trajet trajet) {
        Trajet saved = trajetService.addTrajet(trajet);
        return ResponseEntity.ok(saved);
    }

    }

