package com.example.turaiagent.controllers;

import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.models.Offer;
import com.example.turaiagent.services.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class AgentController {

    AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/register")
    public ResponseEntity<Agent> registerAgent(@RequestBody Agent agent) {
        return ResponseEntity.ok(agentService.registerAgent(agent));
    }

    @GetMapping("/archive/{agentId}/{requestId}")
    public ResponseEntity<Archive> moveToArchive(@PathVariable Long agentId, @PathVariable Long requestId) {
        return ResponseEntity.ok(agentService.moveToArchive(agentId, requestId));
    }

    @GetMapping("/archive/{agentId}")
    public ResponseEntity<List<Archive>> findAllByAgentId(@PathVariable Long agentId) {
        return ResponseEntity.ok(agentService.getArchiveList(agentId));
    }

    @GetMapping("/offered/{agentId}")
    public ResponseEntity<List<AgentRequest>> getOfferedRequests(@PathVariable Long agentId) {
        String i = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        System.out.println("AUTH: " + i);
        return ResponseEntity.ok(agentService.getOfferedRequests(agentId));
    }

    @PostMapping("/offered/{agentId}")
    public ResponseEntity<Offer> getRequestOffer(@PathVariable Long agentId, @RequestBody Offer offer) {
        return ResponseEntity.ok(agentService.createOffer(offer, agentId));
    }
}
