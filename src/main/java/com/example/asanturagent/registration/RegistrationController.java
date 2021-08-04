package com.example.asanturagent.registration;

import com.example.asanturagent.util.jwt.JwtRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register/user")
    public String register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request);
    }

    @GetMapping("/confirm/{token}")
    public String confirm(@PathVariable String token) {
        return registrationService.confirmToken(token);
    }

    @RequestMapping(value = "/authenticate/user", method = RequestMethod.POST)
    public ResponseEntity<String> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        return ResponseEntity.ok(registrationService.authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
    }

}
