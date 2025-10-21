package com.example.emiraia.controllers;

import com.example.emiraia.dto.TrajetDTO;
import com.example.emiraia.entities.Reservation;
import com.example.emiraia.entities.Trajet;
import com.example.emiraia.services.LLamaService;
import com.example.emiraia.services.NLPService;
import com.example.emiraia.services.TrajetService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
public class TrajetChatController {

    private final TrajetService trajetService;
    private final NLPService nlpService;
    private final LLamaService llamaService;

    private final Map<String, ConversationState> sessions = new HashMap<>();

    public TrajetChatController(TrajetService trajetService,
                                NLPService nlpService,
                                LLamaService llamaService) {
        this.trajetService = trajetService;
        this.nlpService = nlpService;
        this.llamaService = llamaService;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request,
                                    @RequestParam String sessionId) {

        String userText = request.get("text");
        ConversationState state = sessions.getOrDefault(sessionId, new ConversationState());

        // --- NLP extraction ---
        Map<String, Object> nlpResult = nlpService.parseRequest(userText);
        String depart = (String) nlpResult.get("depart");
        String arrivee = (String) nlpResult.get("arrivee");
        LocalDateTime latestArriveeTime = null;
        if (nlpResult.get("latestArriveeTime") != null) {
            try {
                latestArriveeTime = LocalDateTime.parse((String) nlpResult.get("latestArriveeTime"));
            } catch (Exception ignored) {}
        }
        int places = (int) nlpResult.getOrDefault("places", 1);
        String intent = (String) nlpResult.getOrDefault("intent", "search");
        int index = (int) nlpResult.getOrDefault("index", 0);
        String strategy = (String) nlpResult.get("strategy");

        // --- Search trajets ---
        List<Trajet> trajets = new ArrayList<>();
        if (depart != null && arrivee != null) {
            trajets = trajetService.searchTrajetsRaw(depart, arrivee, latestArriveeTime, places);
            state.setLastSearchResults(trajets);
            state.setDepart(depart);
            state.setArrivee(arrivee);
            state.setPendingSeats(places);
            sessions.put(sessionId, state);

            // Apply strategy manually (no comparator)
            if (strategy != null && !trajets.isEmpty()) {
                if ("cheapest".equalsIgnoreCase(strategy)) {
                    Trajet cheapest = trajets.get(0);
                    for (Trajet t : trajets) {
                        if (t.getPrixBase() < cheapest.getPrixBase()) {
                            cheapest = t;
                        }
                    }
                    trajets.remove(cheapest);
                    trajets.add(0, cheapest);
                } else if ("earliest".equalsIgnoreCase(strategy)) {
                    Trajet earliest = trajets.get(0);
                    for (Trajet t : trajets) {
                        if (t.getHeureDepart().isBefore(earliest.getHeureDepart())) {
                            earliest = t;
                        }
                    }
                    trajets.remove(earliest);
                    trajets.add(0, earliest);
                }
            }
        }

        // --- Handle reservation ---
        Reservation reservation = null;
        Trajet chosenTrajet = null;
        if (!state.getLastSearchResults().isEmpty() && "reserve".equalsIgnoreCase(intent)) {
            int chosenIndex = index < state.getLastSearchResults().size() ? index : 0;
            chosenTrajet = state.getLastSearchResults().get(chosenIndex);
            reservation = trajetService.reserverTrajet(chosenTrajet.getId(), "passengerId123", state.getPendingSeats());
        }

        // --- Build LLaMA prompt ---
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("User said: \"").append(userText).append("\"\n");
        if (!trajets.isEmpty() && reservation == null) {
            promptBuilder.append("I found the following trips for you:\n");
            for (int i = 0; i < trajets.size(); i++) {
                Trajet t = trajets.get(i);
                promptBuilder.append(String.format("%d. From: %s → To: %s, Price: €%.2f, Seats: %d\n",
                        i + 1, t.getDepart(), t.getArrivee(), t.getPrixBase(), t.getPlacesDisponibles()));
            }
            promptBuilder.append("Do NOT assume reservation, only suggest options.\n");
        } else if (reservation != null) {
            promptBuilder.append(String.format(
                    "Reservation confirmed for %s → %s at %s. Price: €%.2f, Seats: %d\n",
                    chosenTrajet.getDepart(), chosenTrajet.getArrivee(),
                    chosenTrajet.getHeureDepart(), reservation.getPrixFinal(),
                    reservation.getPlaces()
            ));
        } else {
            promptBuilder.append("You are TrajetChat, a friendly travel assistant. Greet the user and offer help.\n");
        }

        String botMessage = llamaService.generateMessage(promptBuilder.toString());

        // --- Build response ---
        Map<String, Object> response = new HashMap<>();
        response.put("message", botMessage);
        response.put("trajets", trajets);
        if (reservation != null) response.put("reservation", reservation);

        return response;
    }

    // --- Conversation state ---
    private static class ConversationState {
        private List<Trajet> lastSearchResults = new ArrayList<>();
        private String depart;
        private String arrivee;
        private int pendingSeats;

        public List<Trajet> getLastSearchResults() { return lastSearchResults; }
        public void setLastSearchResults(List<Trajet> lastSearchResults) {
            this.lastSearchResults = lastSearchResults != null ? lastSearchResults : new ArrayList<>();
        }
        public String getDepart() { return depart; }
        public void setDepart(String depart) { this.depart = depart; }
        public String getArrivee() { return arrivee; }
        public void setArrivee(String arrivee) { this.arrivee = arrivee; }
        public int getPendingSeats() { return pendingSeats; }
        public void setPendingSeats(int pendingSeats) { this.pendingSeats = pendingSeats; }
    }
}
