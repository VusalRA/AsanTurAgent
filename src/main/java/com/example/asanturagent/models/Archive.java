package com.example.asanturagent.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "archives")
@Builder(toBuilder = true)
@Entity
public class Archive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "agent_id")
    private Long agentId;
    @Column(name = "request_id")
    private Long requestId;
    private String status;

    public Archive(AgentRequest agentRequest) {
        this.agentId = agentRequest.getAgentId();
        this.status = agentRequest.getStatus();
    }
}
