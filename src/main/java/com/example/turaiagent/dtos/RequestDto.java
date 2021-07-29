package com.example.turaiagent.dtos;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class RequestDto {

    private String uuid;
    private Map<String, String> data;

}
