package com.example.turaiagent.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class AcceptDto {

    private String uuid;
    private String email;
    private String phoneNumber;

}
