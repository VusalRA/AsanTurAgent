package com.example.asanturagent.models;

import com.example.asanturagent.enums.AppUserRole;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table(name = "agents")
@Entity
public class Agent implements UserDetails {

    @SequenceGenerator(
            name = "agentUser_sequence",
            sequenceName = "agent_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "agent_sequence"
    )
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @Column(name = "company_name")
    private String companyName;
    private String password;
    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;
    private Boolean locked = false;
    private Boolean enabled = false;

    public Agent(String firstName,
                 String lastName,
                 String email,
                 String password,
                 String companyName,
                 AppUserRole appUserRole) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.companyName = companyName;
        this.appUserRole = appUserRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

//    public String getFirstName() {
//        return firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
