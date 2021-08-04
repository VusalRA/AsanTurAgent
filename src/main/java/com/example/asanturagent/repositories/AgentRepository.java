package com.example.asanturagent.repositories;

import com.example.asanturagent.models.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query("SELECT agent FROM Agent agent where agent.email=:email")
    Agent findUserByEmail(String email);

    Optional<Agent> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE Agent a " +
            "SET a.enabled = TRUE WHERE a.email = ?1")
    int enableAppUser(String email);


}
