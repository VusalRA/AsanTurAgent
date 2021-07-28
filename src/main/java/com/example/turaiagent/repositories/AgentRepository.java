package com.example.turaiagent.repositories;

import com.example.turaiagent.models.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Agent findByEmail(String name);

}
