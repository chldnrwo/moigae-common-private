package com.moigae.application.component.user.dto;

import com.moigae.application.component.user.domain.Address;
import com.moigae.application.component.user.domain.User;
import com.moigae.application.component.user.domain.enumeration.Gender;
import com.moigae.application.component.user.domain.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Data
public class UserDto implements UserDetails {
    private String id;
    private String userName;
    private String password;
    private Gender gender;
    private String phone;
    private LocalDate birth;
    private Address address;
    private String account;
    private String hostIntroduction;
    private String email;
    private UserRole userRole;

    public UserDto(){}
    public static UserDto of(User user) {
        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getPassword(),
                user.getGender(),
                user.getPhone(),
                user.getBirth(),
                user.getAddress(),
                user.getAccount(),
                user.getHostIntroduction(),
                user.getEmail(),
                user.getUserRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.getUserRole().getValue()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}