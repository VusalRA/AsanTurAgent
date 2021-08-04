package com.example.asanturagent.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class OfferDto {

    private Long id;
    private String uuid;
    private String companyName;
    private String email;
    private byte[] file;

}
