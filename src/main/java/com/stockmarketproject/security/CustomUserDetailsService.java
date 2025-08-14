package com.stockmarketproject.security;

import com.stockmarketproject.entity.User;
import com.stockmarketproject.entity.Role; // <-- enum Role { ADMIN, USER }
import com.stockmarketproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.stockmarketproject.entity.User u = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // User entity’de role alanı enum Role ise:
        Role role = u.getRole();                // null ise default USER verelim
        String roleName = (role != null) ? role.name() : "USER";

        // User entity’de enabled alanı yoksa, tüm bayrakları true yap
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }
}
