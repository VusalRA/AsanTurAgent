package com.example.turaiagent.controllers;

import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.services.AgentServive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class AgentController {


    AgentServive agentServive;

    public AgentController(AgentServive agentServive) {
        this.agentServive = agentServive;
    }

    @PostMapping("/register")
    public ResponseEntity<Agent> registerAgent(@RequestBody Agent agent) {
        return ResponseEntity.ok(agentServive.registerAgent(agent));
    }

    @GetMapping("/archive/{agentId}/{requestId}")
    public ResponseEntity<Archive> moveToArchive(@PathVariable Long agentId, @PathVariable Long requestId) {
        return ResponseEntity.ok(agentServive.moveToArchive(agentId, requestId));
    }

    @GetMapping("/archive/{agentId}")
    public ResponseEntity<List<Archive>> findAllByAgentId(@PathVariable Long agentId) {
        return ResponseEntity.ok(agentServive.getArchiveList(agentId));
    }

    @GetMapping("/offered/{agentId}")
    public ResponseEntity<List<AgentRequest>> getOfferedRequests(@PathVariable Long agentId) {
        return ResponseEntity.ok(agentServive.getOfferedRequests(agentId));
    }
}
