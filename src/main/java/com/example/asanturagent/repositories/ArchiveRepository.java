package com.example.asanturagent.repositories;

import com.example.asanturagent.models.Archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    List<Archive> findAllByAgentId(Long agentId);

}
