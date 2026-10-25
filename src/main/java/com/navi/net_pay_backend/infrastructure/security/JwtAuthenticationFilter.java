package com.navi.net_pay_backend.infrastructure.security;

import com.navi.net_pay_backend.application.use_case.TokenCredentialUseCase;
import com.navi.net_pay_backend.application.use_case.AccessControlUseCase;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.service.model.TokenPayload;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenCredentialUseCase tokenCredentialUseCase;
    private final AccessControlUseCase accessControlUseCase;
    private final AppUserRepository users;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                TokenPayload payload = tokenCredentialUseCase.verify(token);

                if (payload != null && payload.getUserId() != null && users
                        .findByIdAndStatusNot(payload.getUserId(), TypologyEnum.DELETED.getId())
                        .filter(user -> user.getTpStatus() != null && TypologyEnum.ACTIVE.getId().equals(user.getTpStatus().getInternalId()))
                        .isPresent()) {
                    // Guardamos el userId como el "principal" (identidad primaria del hilo)
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            payload.getUserId(),
                            null,
                            accessControlUseCase.effectivePermissions(payload.getUserId()).stream()
                                    .map(SimpleGrantedAuthority::new).toList()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
