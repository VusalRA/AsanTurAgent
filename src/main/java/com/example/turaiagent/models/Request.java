package com.example.turaiagent.models;

import lombok.*;

import javax.persistence.*;

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
}
