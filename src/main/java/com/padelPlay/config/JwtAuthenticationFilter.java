package com.padelPlay.config;

import com.padelPlay.entity.Administrateur;
import com.padelPlay.entity.Membre;
import com.padelPlay.repository.AdministrateurRepository;
import com.padelPlay.repository.MembreRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final AdministrateurRepository administrateurRepository;
    private final MembreRepository membreRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // si pas de token → on laisse passer (les routes publiques sont gérées dans SecurityConfig)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // enlève "Bearer "

        if (!jwtConfig.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String subject = jwtConfig.extractEmail(token); // peut être un email (Admin) ou un matricule (Membre)
        String role = jwtConfig.extractRole(token);

        // Distinguer admin et membre par le format du subject
        boolean isAdmin = subject.contains("@"); // email = admin, matricule = membre

        if (isAdmin) {
            Administrateur admin = administrateurRepository.findByEmail(subject).orElse(null);
            if (admin != null) {
                authenticate(subject, role);
                log.debug("Admin {} authenticated with role {}", subject, role);
            }
        } else {
            Membre membre = membreRepository.findByMatricule(subject).orElse(null);
            if (membre != null) {
                authenticate(subject, role);
                log.debug("Member {} authenticated with role {}", subject, role);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String principal, String role) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}