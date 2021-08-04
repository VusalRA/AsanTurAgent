package com.example.asanturagent.repositories;

import com.example.asanturagent.models.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface AppUserRepository
        extends JpaRepository<Agent, Long> {

    Optional<Agent> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE Agent a " +
            "SET a.enabled = TRUE WHERE a.email = ?1")
    int enableAppUser(String email);

    Agent findUserByEmail(String email);

}
