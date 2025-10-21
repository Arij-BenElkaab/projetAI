package com.example.emiraia.services;

import com.example.emiraia.Repositories.ReservationRepository;
import com.example.emiraia.Repositories.TrajetRepository;
import com.example.emiraia.dto.TrajetDTO;
import com.example.emiraia.entities.Reservation;
import com.example.emiraia.entities.Trajet;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class TrajetService {

    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;

    public TrajetService(TrajetRepository trajetRepository,
                         ReservationRepository reservationRepository) {
        this.trajetRepository = trajetRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TrajetDTO> searchTrajets(String depart, String arrivee,
                                         LocalDateTime latestArriveeTime, int places) {
        return trajetRepository
                .findByDepartAndArriveeAndPlacesDisponiblesGreaterThanEqual(depart, arrivee, places)
                .stream()
                .filter(t -> latestArriveeTime == null || !t.getHeureDepart().isAfter(latestArriveeTime))
                .map(t -> new TrajetDTO(t, latestArriveeTime))
                .sorted((a, b) -> Long.compare(a.getMinutesBeforeDeadline(), b.getMinutesBeforeDeadline()))
                .toList();
    }

    public Reservation reserverTrajet(String trajetId, String passengerId, int places) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        if (trajet.getPlacesDisponibles() < places) {
            throw new RuntimeException("Pas assez de places disponibles");
        }

        // Update available seats first
        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - places);
        trajetRepository.save(trajet);

        // Then create reservation
        double prixFinal = trajet.getPrixBase() * places;
        Reservation reservation = new Reservation(trajetId, passengerId, places, prixFinal);
        return reservationRepository.save(reservation);
    }

    public List<TrajetDTO> applyStrategy(List<TrajetDTO> trajets, String strategy) {
        if ("earliest".equalsIgnoreCase(strategy)) {
            trajets.sort(Comparator.comparing(t -> t.getTrajet().getHeureDepart()));
        } else if ("cheapest".equalsIgnoreCase(strategy)) {
            trajets.sort(Comparator.comparing(TrajetDTO::getPrixFinal));
        }
        return trajets;
    }

    // Add a new Trajet
    public Trajet addTrajet(Trajet trajet) {
        // Optional: validate fields
        if (trajet.getDepart() == null || trajet.getArrivee() == null) {
            throw new RuntimeException("Depart and Arrivee must be provided");
        }
        if (trajet.getPlacesDisponibles() <= 0) {
            trajet.setPlacesDisponibles(1);
        }
        if (trajet.getPrixBase() < 0) {
            trajet.setPrixBase(0);
        }
        return trajetRepository.save(trajet);
    }
    // Return raw Trajet entities instead of DTOs
    public List<Trajet> searchTrajetsRaw(String depart, String arrivee, LocalDateTime latestArriveeTime, int places) {
        List<Trajet> trajets = trajetRepository
                .findByDepartAndArriveeAndPlacesDisponiblesGreaterThanEqual(depart, arrivee, places);

        if (latestArriveeTime != null) {
            trajets = trajets.stream()
                    .filter(t -> !t.getHeureDepart().isAfter(latestArriveeTime))
                    .toList();
        }

        return trajets;
    }

}


