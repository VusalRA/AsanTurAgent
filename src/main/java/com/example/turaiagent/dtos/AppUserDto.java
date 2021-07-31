package com.example.turaiagent.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)

public class AppUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String password;
}
