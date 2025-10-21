package tn.esprit.ia.covoiturage.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
    public class CovService {

        private final RestTemplate restTemplate = new RestTemplate();
        private final String flaskUrl = "http://127.0.0.1:5000/predict";

    public String predict(Map<String, Object> data) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, data, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("message")) {
                return response.getBody().get("message").toString();
            } else {
                return "No prediction message returned from Flask.";
            }
        } catch (Exception e) {
            return "Error while connecting to Flask API: " + e.getMessage();
        }
    }


}
