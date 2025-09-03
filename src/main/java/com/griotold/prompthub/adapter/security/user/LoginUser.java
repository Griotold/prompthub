package com.griotold.prompthub.adapter.security.user;

import com.griotold.prompthub.domain.member.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class LoginUser implements UserDetails {
    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + member.getRole().name());
    }

    @Override
    public String getPassword() {
        return "SOCIAL_LOGIN";
    }

    @Override
    public String getUsername() {
        return member.getEmail().address();
    }
}
