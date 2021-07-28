package com.example.turaiagent.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    @Column(name = "request_end_date")
    private LocalDateTime requestEndDate;
}
