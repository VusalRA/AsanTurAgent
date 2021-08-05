package com.example.asanturagent.services;

import com.example.asanturagent.dtos.AcceptDto;
import com.example.asanturagent.dtos.RequestDto;
import com.example.asanturagent.models.Agent;
import com.example.asanturagent.models.AgentRequest;
import com.example.asanturagent.models.Offer;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface AgentService {

    List<AgentRequest> getRequests(Long agentId);

    AgentRequest moveToArchive(Long agentId, Long requestId);

    List<AgentRequest> getArchiveList(Long agentId);

//    List<AgentRequest> getOfferedRequests(Long agentId);

//    List<AgentRequest> getOfferedRequestsByEmail(String email);


    Offer createOffer(Offer offer, Long agentId);

    Agent getAgentFromToken() throws JsonProcessingException;

    Agent resetPassword(String oldPassword, String repeatPassword);

    Agent forgotPassword(String email);

    String confirm(Integer password);

    void changePassword(String newPassword, Agent agent);

    Agent findAgent(String email);

    void listenStopQueue(String uuid);

    void listenRequestQueue(RequestDto requestDto);

    void listenAcceptQueue(AcceptDto acceptDto);

    void checkRequestEndTime();

    List<AgentRequest> getAcceptRequestsByEmail(String email);

    List<AgentRequest> getAllRequest(String email, String Status);
}
