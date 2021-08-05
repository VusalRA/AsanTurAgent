package com.example.asanturagent.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requests")
@Builder(toBuilder = true)
@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String data;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "request_date_time")
    private LocalDateTime requestDateTime;

    @Column(name = "request_end_date_time")
    private LocalDateTime requestEndDateTime;

    @OneToMany
    @JoinColumn(name = "agent_id")
    private List<Agent> agents;

}
