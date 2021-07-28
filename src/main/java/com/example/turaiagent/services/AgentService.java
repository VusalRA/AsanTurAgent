package com.example.turaiagent.services;

import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.models.Offer;

import java.util.List;

public interface AgentService {

    Agent registerAgent(Agent agent);

    Archive moveToArchive(Long agentId, Long requestId);

    List<Archive> getArchiveList(Long agentId);

    List<AgentRequest> getOfferedRequests(Long agentId);

    Offer createOffer(Offer offer, Long agentId);

}
