package com.example.turaiagent.repositories;

import com.example.turaiagent.models.Archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    List<Archive> findAllByAgentId(Long agentId);

}
