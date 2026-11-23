package com.tecnicodomicilio.servicio_tecnico_backend.config;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String correo = jwtUtil.extraerCorreo(token);

                if (correo != null
                        && SecurityContextHolder.getContext().getAuthentication() == null
                        && jwtUtil.esTokenValido(token, correo)) {

                    Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);

                    if (usuario.isPresent() && "ACTIVO".equals(usuario.get().getEstado())) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                correo, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.get().getRol()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else if (usuario.isPresent() && "BLOQUEADO".equals(usuario.get().getEstado())) {
                        request.setAttribute("cuentaBloqueada", true);
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}