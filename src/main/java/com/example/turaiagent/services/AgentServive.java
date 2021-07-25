package com.example.turaiagent.services;

import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;

import java.util.List;

public interface AgentServive {

    Agent registerAgent(Agent agent);

    Archive moveToArchive(Long agentId, Long requestId);

    List<Archive> getArchiveList(Long agentId);

    List<AgentRequest> getOfferedRequests(Long agentId);

}
