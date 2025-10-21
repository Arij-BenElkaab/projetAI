package com.example.emiraia.Repositories;

import com.example.emiraia.entities.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Users, String> {

}
