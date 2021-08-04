package com.example.asanturagent.repositories;

import com.example.asanturagent.models.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {

    Optional<ForgotPassword> findByRandom(Integer random);

}
