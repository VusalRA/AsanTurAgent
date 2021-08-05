package com.example.asanturagent.services;

import com.example.asanturagent.exceptions.EmailAlreadyTakenException;
import com.example.asanturagent.models.Agent;
import com.example.asanturagent.registration.token.ConfirmationToken;
import com.example.asanturagent.registration.token.ConfirmationTokenService;
import com.example.asanturagent.repositories.AgentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AgentRegistrationService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
            "user with email %s not found";

    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return agentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(Agent agent) {
        boolean userExists = agentRepository
                .findByEmail(agent.getEmail())
                .isPresent();
        if (userExists) {
            // TODO check of attributes are the same and
            // TODO if email not confirmed send confirmation email.
            throw new EmailAlreadyTakenException();
        }
        String encodedPassword = bCryptPasswordEncoder
                .encode(agent.getPassword());
        agent.setPassword(encodedPassword);
        agentRepository.save(agent);
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                agent
        );
        confirmationTokenService.saveConfirmationToken(
                confirmationToken);
//        TODO: SEND EMAIL
        return token;
    }

    public int enableAppUser(String email) {
        return agentRepository.enableAppUser(email);
    }
}
