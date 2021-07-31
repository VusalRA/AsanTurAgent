package com.example.turaiagent.registration;

import com.example.turaiagent.models.Agent;
import com.example.turaiagent.repositories.AgentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private AgentRepository agentRepo;

    public JwtUserDetailsService(AgentRepository agentRepo) {
        this.agentRepo = agentRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Agent user = agentRepo.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }
}