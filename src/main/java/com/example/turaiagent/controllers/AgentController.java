package com.example.turaiagent.controllers;

import com.example.turaiagent.models.*;
import com.example.turaiagent.services.AgentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class AgentController {

    AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

//    @PostMapping("/register")
//    public ResponseEntity<Agent> registerAgent(@RequestBody Agent agent) {
//        return ResponseEntity.ok(agentService.registerAgent(agent));
//    }

//    @PostMapping("/register")
//    public ResponseEntity<AppUserDto> registerAgent(@RequestBody AppUserDto appUserDto) {
//        return ResponseEntity.ok(agentService.registerUser(appUserDto));
//    }

    @GetMapping("/archive/{requestId}")
    public ResponseEntity<Archive> moveToArchive(@PathVariable Long requestId) throws JsonProcessingException {
        return ResponseEntity.ok(agentService.moveToArchive(agentService.getFromToken().getId(), requestId));
    }

    @GetMapping("/archive")
    public ResponseEntity<List<Archive>> findAllByAgentId() throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getArchiveList(agentService.getFromToken().getId()));
    }

    @PostMapping("/offered")
    public ResponseEntity<Offer> getRequestOffer(@RequestBody Offer offer) throws JsonProcessingException {
//        Agent agent = agentService.getFromToken();

        return ResponseEntity.ok(agentService.createOffer(offer, agentService.getFromToken().getId()));
    }

    @GetMapping("/offered")
    public ResponseEntity<List<AgentRequest>> getOfferedRequests() throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getOfferedRequestsByEmail(agentService.getFromToken().getEmail()));
    }

    @PostMapping("/reset")
    public ResponseEntity<Agent> getResetPassword(@RequestBody ResetPassword resetPassword) {
        return ResponseEntity.ok(agentService.resetPassword(resetPassword));
    }
}
