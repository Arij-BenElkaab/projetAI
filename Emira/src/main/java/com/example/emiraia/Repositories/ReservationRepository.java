package com.example.emiraia.Repositories;

import com.example.emiraia.entities.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
}
