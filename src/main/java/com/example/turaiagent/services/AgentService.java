package com.example.turaiagent.services;

import com.example.turaiagent.dtos.ResetPasswordDto;
import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.models.Offer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AgentService {

    Archive moveToArchive(Long agentId, Long requestId);

    List<Archive> getArchiveList(Long agentId);

    List<AgentRequest> getOfferedRequests(Long agentId);

    Offer createOffer(Offer offer, Long agentId);

    Agent getFromToken() throws JsonProcessingException;

    List<AgentRequest> getOfferedRequestsByEmail(String email);

    Agent resetPassword(ResetPasswordDto resetPasswordDto);

    Agent forgotPassword(String email);

    String confirmToken(String token);

    String confirm(Integer password);

    void changePassword(String newPassword, Agent agent);

    Agent findAgent(String email);

}
