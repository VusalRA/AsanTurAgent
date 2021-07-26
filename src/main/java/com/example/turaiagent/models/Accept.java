package com.example.turaiagent.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class Accept {

    private String uuid;
    private String companyName;
    private String phoneNumber;

}
