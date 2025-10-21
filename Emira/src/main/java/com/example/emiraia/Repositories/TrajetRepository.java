package com.example.emiraia.Repositories;
import com.example.emiraia.entities.Trajet;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TrajetRepository extends MongoRepository<Trajet, String> {
    List<Trajet> findByDepartAndArrivee(String depart, String arrivee);


    List<Trajet> findByDepartAndArriveeAndPlacesDisponiblesGreaterThanEqual(String depart, String arrivee, int places);
}