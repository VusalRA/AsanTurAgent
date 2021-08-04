package com.example.turaiagent.repositories;

import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    AgentRequest findByAgentIdAndRequestId(Long agentId, Long requestId);

    @Query("SELECT agentRequest FROM AgentRequest agentRequest where agentRequest.agentId=:agentId and agentRequest.status='OFFERED'")
    List<AgentRequest> findAllByAgentId(Long agentId);
//and agentRequest.status='OFFERED'
//    List<AgentRequest> findAllByAgentId

    //    @Query("SELECT agentRequest FROM AgentRequest agentRequest where agentRequest.agentId=:agentId and agentRequest.requestId=:requestId and agentRequest.status='NEW_REQUEST'")
    Boolean existsByAgentIdAndRequestIdAndStatus(Long agentId, Long requestId, String status);

    @Query("SELECT agentRequest FROM AgentRequest  agentRequest where agentRequest.agentId=:agentId")
    List<AgentRequest> findAllByAgentIdWithout(Long agentId);

    AgentRequest findByRequest(Request request);

    @Query("SELECT agentRequest FROM AgentRequest agentRequest where agentRequest.agentId=:agentId and agentRequest.status='ACCEPT'")
    List<AgentRequest> findAllByAgentIdAndAccept(Long agentId);

    List<AgentRequest> findAllByAgentIdAndArchiveTrue(Long agentId);

}
