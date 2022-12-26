package com.microtimemanagement.apiservice.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mtm_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel implements UserDetails {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String username;

    private String uid;

    private Set<String> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return super.getIsActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.getIsActive();
    }

    @Override
    public boolean isEnabled() {
        return super.getIsActive();
    }
}
