package com.example.turaiagent.repositories;

import com.example.turaiagent.models.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByUuid(String uuid);

}
