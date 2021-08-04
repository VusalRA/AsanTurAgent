package com.example.turaiagent.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_request")
@Entity
@Builder(toBuilder = true)

public class AgentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "agent_id")
    private Long agentId;
    //    @Column(name = "request_id")
//    private Long requestId;
    private String status;
    @Column(name = "phone_number")
    private String phoneNumber;

    private Boolean archive;

    @ManyToOne(targetEntity = Request.class)
    private Request request;

}
