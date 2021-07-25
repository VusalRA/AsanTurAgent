package com.example.turaiagent.repositories;

import com.example.turaiagent.models.AgentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    AgentRequest findByAgentIdAndRequestId(Long agentId, Long requestId);

    @Query("SELECT agentRequest FROM AgentRequest agentRequest where agentRequest.agentId=:agentId and agentRequest.status='OFFERED'")
    List<AgentRequest> findAllByAgentId(Long agentId);

}
