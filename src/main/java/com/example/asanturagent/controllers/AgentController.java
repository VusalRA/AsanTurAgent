package com.example.asanturagent.controllers;

import com.example.asanturagent.dtos.NewPassword;
import com.example.asanturagent.models.Agent;
import com.example.asanturagent.models.AgentRequest;
import com.example.asanturagent.models.Offer;
import com.example.asanturagent.services.AgentService;
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

    @GetMapping("/requests/{status}")
    public ResponseEntity<List<AgentRequest>> getAllRequests(@PathVariable String status) throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getAllRequest(agentService.getFromToken().getEmail(), status.toUpperCase()));
    }

    @GetMapping("/archive/{requestId}")
    public ResponseEntity<AgentRequest> moveToArchive(@PathVariable Long requestId) throws JsonProcessingException {
        return ResponseEntity.ok(agentService.moveToArchive(agentService.getFromToken().getId(), requestId));
    }

    @GetMapping("/archive")
    public ResponseEntity<List<AgentRequest>> findAllByAgentId() throws JsonProcessingException {
        return ResponseEntity.ok(agentService.getArchiveList(agentService.getFromToken().getId()));
    }

    @PostMapping("/offered")
    public ResponseEntity<Offer> createRequestOffer(@RequestBody Offer offer) throws JsonProcessingException {
        return ResponseEntity.ok(agentService.createOffer(offer, agentService.getFromToken().getId()));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> getResetPassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        agentService.resetPassword(oldPassword, newPassword);
        return ResponseEntity.ok("Your password has been changed. New password is: " + newPassword);
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
