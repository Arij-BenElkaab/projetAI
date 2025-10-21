package tn.esprit.ia.covoiturage.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ia.covoiturage.dto.PredictionRequestDTO;
import tn.esprit.ia.covoiturage.services.CovService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ia")
@CrossOrigin("http://localhost:4200")
public class CovController {

    private final CovService covService;

    public CovController(CovService covService) {
        this.covService = covService;
    }

    @PostMapping("/predict")
    public ResponseEntity<String> predict(@RequestBody PredictionRequestDTO dto) {
        Map<String, Object> data = new HashMap<>();
        data.put("trip_distance", dto.getTripDistance());
        data.put("pickup_distance_km", dto.getPickupDistanceKm());
        data.put("price", dto.getPrice());
        data.put("trip_seconds", dto.getTripSeconds());

        String result = covService.predict(data);
        return ResponseEntity.ok(result);
    }
}