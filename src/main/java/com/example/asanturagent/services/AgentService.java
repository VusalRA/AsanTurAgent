package com.example.asanturagent.services;

import com.example.asanturagent.dtos.ResetPasswordDto;
import com.example.asanturagent.dtos.StopDto;
import com.example.asanturagent.models.Agent;
import com.example.asanturagent.models.AgentRequest;
import com.example.asanturagent.models.Offer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AgentService {

    List<AgentRequest> getRequests(Long agentId);

    AgentRequest moveToArchive(Long agentId, Long requestId);

    List<AgentRequest> getArchiveList(Long agentId);

    List<AgentRequest> getOfferedRequests(Long agentId);

    Offer createOffer(Offer offer, Long agentId);

    Agent getFromToken() throws JsonProcessingException;

    List<AgentRequest> getOfferedRequestsByEmail(String email);

    Agent resetPassword(ResetPasswordDto resetPasswordDto);

    Agent forgotPassword(String email);

    String confirm(Integer password);

    void changePassword(String newPassword, Agent agent);

    Agent findAgent(String email);

    void stopRequest(StopDto stopDto);

    void checkRequestEndTime();

    List<AgentRequest> getAcceptRequestsByEmail(String email);

    List<AgentRequest> getAllRequest(String email, String Status);
}
