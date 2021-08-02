package com.example.turaiagent.controllers;

import com.example.turaiagent.dtos.NewPassword;
import com.example.turaiagent.dtos.ResetPasswordDto;
import com.example.turaiagent.models.Agent;
import com.example.turaiagent.models.AgentRequest;
import com.example.turaiagent.models.Archive;
import com.example.turaiagent.models.Offer;
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

    @GetMapping("/requests")
    public ResponseEntity<List<AgentRequest>> getRequests() throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getRequests(agentService.getFromToken().getId()));
    }

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
        return ResponseEntity.ok(agentService.createOffer(offer, agentService.getFromToken().getId()));
    }

    @GetMapping("/offered")
    public ResponseEntity<List<AgentRequest>> getOfferedRequests() throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getOfferedRequestsByEmail(agentService.getFromToken().getEmail()));
    }

    @PostMapping("/reset")
    public ResponseEntity<Agent> getResetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return ResponseEntity.ok(agentService.resetPassword(resetPasswordDto));
    }

    @GetMapping("/forgot/{email}")
    public void forgotPassword(@PathVariable String email) {
        agentService.forgotPassword(email);
    }

//    @GetMapping("/forgot/confirm/{token}")
//    public String forgotConfirm(@PathVariable String token) {
//        return agentService.confirmToken(token);
//    }
//

    @PostMapping("/forgot/{password}")
    public String forgotPasswordConfirm(@PathVariable Integer password, @RequestBody NewPassword newPassword) {
        System.out.println("FORGOT: " + agentService.confirm(password));

        Agent agent = agentService.findAgent(agentService.confirm(password));

        System.out.println(agent.getEmail());
        agentService.changePassword(newPassword.getPassword(), agent);
        return "Changed";
    }


}
