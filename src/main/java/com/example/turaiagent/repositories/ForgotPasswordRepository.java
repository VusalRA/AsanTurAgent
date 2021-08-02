package com.example.turaiagent.repositories;

import com.example.turaiagent.models.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {

    Optional<ForgotPassword> findByRandom(Integer random);

}
