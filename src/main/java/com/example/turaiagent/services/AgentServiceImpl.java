package com.example.turaiagent.services;

import com.example.turaiagent.enums.Status;
import com.example.turaiagent.models.*;
import com.example.turaiagent.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    OfferRepository offerRepo;

    public AgentServiceImpl(AgentRepository agentRepo, RequestRepository requestRepo, AgentRequestRepository agentRequestRepo, ArchiveRepository archiveRepo, OfferRepository offerRepo) {
        this.agentRepo = agentRepo;
        this.requestRepo = requestRepo;
        this.agentRequestRepo = agentRequestRepo;
        this.archiveRepo = archiveRepo;
        this.offerRepo = offerRepo;
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

    @Override
    public Offer createOffer(Offer offer, Long agentId) {
        Request request = requestRepo.findByUuid(offer.getUuid());
        if (agentRequestRepo.existsByAgentIdAndRequestIdAndStatus(agentId, request.getId(), "NEW_REQUEST")) {
            offerRepo.save(offer);
            System.out.println("Success");
            AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, request.getId());
            agentRequest.setStatus(Status.OFFERED.name());
            agentRequestRepo.save(agentRequest);
        }
        return offer;
    }

    @RabbitListener(queues = "accept_queue")
    public void listenAccept(Object accept) throws JsonProcessingException {
//        System.out.println(accept.getCompanyName());
        String data = Arrays.asList(accept.toString().split("'")).get(1);
//        System.out.println(data);
        ObjectMapper objectMapper = new ObjectMapper();
        Accept accept1 = objectMapper.readValue(data, Accept.class);
        Agent agent = agentRepo.findByCompanyName(accept1.getCompanyName());
//Request request=requestRepo.

    }

    //    @PostConstruct
    public String getRequest(String data) throws JsonProcessingException {
        List<Request> requests = requestRepo.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
//
//        requests.forEach(request -> {
        JsonNode node = null;
        try {
            node = objectMapper.readValue(data, JsonNode.class);
//            System.out.println(node.get("select").textValue());
            System.out.println(node.get("uuid").textValue());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return node.get("uuid").textValue();

//        });

//        JsonNode array = objectMapper.readValue(request.getData(), JsonNode.class);
//        System.out.println(array.toString());
//        String request2 = array.get("uuid").textValue();
//        System.out.println(request2);


//        try {
//            RequestTest requestTest = objectMapper.readValue(request.getData(), RequestTest.class);
//            System.out.println(requestTest.getTravellerCount());
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            System.out.println("Error with json");
//        }
    }


    @RabbitListener(queues = "request_queue")
    public void listener(Object request) {
        String data = Arrays.asList(request.toString().split("'")).get(1);
        String uuid = null;
        try {
            uuid = getRequest(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Request request1 = requestRepo.save(Request.builder().uuid(uuid).data(data).build());
        List<Agent> agentList = agentRepo.findAll();
        agentList.forEach(agent -> agentRequestRepo
                .save(AgentRequest.builder()
                        .agentId(agent.getId()).requestId(request1.getId())
                        .status(Status.NEW_REQUEST.name()).build()));
    }
}
