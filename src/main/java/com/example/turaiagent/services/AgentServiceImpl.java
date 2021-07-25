package com.example.turaiagent.services;

import com.example.turaiagent.enums.Status;
import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.models.Request;
import com.example.turaiagent.repositories.AgentRepository;
import com.example.turaiagent.repositories.AgentRequestRepository;
import com.example.turaiagent.repositories.ArchiveRepository;
import com.example.turaiagent.repositories.RequestRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AgentServiceImpl implements AgentServive {

    AgentRepository agentRepo;
    RequestRepository requestRepo;
    AgentRequestRepository agentRequestRepo;
    ArchiveRepository archiveRepo;

    public AgentServiceImpl(AgentRepository agentRepo, RequestRepository requestRepo, AgentRequestRepository agentRequestRepo, ArchiveRepository archiveRepo) {
        this.agentRepo = agentRepo;
        this.requestRepo = requestRepo;
        this.agentRequestRepo = agentRequestRepo;
        this.archiveRepo = archiveRepo;
    }

    @Override
    public Agent registerAgent(Agent agent) {
        return agentRepo.save(agent);
    }

    @Override
    public Archive moveToArchive(Long agentId, Long requestId) {
        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, requestId);
        if (agentRequest.getStatus().equals(Status.NEW_REQUEST.name())) {
            agentRequest.setStatus(Status.EXPIRED.name());
        }
        Archive archive = archiveRepo.save(new Archive(agentRequest));
        agentRequestRepo.delete(agentRequest);
        return archive;
    }

    @Override
    public List<Archive> getArchiveList(Long agentId) {
        return archiveRepo.findAllByAgentId(agentId);
    }

    @Override
    public List<AgentRequest> getOfferedRequests(Long agentId) {
        return agentRequestRepo.findAllByAgentId(agentId);
    }

    @RabbitListener(queues = "request_queue")
    public void listener(Object request) {
        String data = Arrays.asList(request.toString().split("'")).get(1);
        Request request1 = requestRepo.save(Request.builder().data(data).build());
        List<Agent> agentList = agentRepo.findAll();
        agentList.forEach(agent -> agentRequestRepo
                .save(AgentRequest.builder()
                        .agentId(agent.getId()).requestId(request1.getId())
                        .status(Status.NEW_REQUEST.name()).build()));
    }
}
