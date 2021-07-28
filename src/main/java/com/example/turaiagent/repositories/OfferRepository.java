package com.example.turaiagent.repositories;

import com.example.turaiagent.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

//    List<Offer> findAllBy

}
