package com.blogplatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImplWrapper {

    private final UserDetailsServiceImpl delegate;

    public UserDetails loadUserByUsername(String username) {
        return delegate.loadUserByUsername(username);
    }
}
