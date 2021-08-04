package com.example.asanturagent.repositories;

import com.example.asanturagent.models.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByUuid(String uuid);

}
