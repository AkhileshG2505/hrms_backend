package com.texlaculture.leave.security;

import com.texlaculture.leave.entity.AppUser;
import com.texlaculture.leave.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final AppUserRepository appUserRepository;

    public AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }
}
