package edu.miu.springsecurity1.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtHelper jwtHelper;

    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtHelper jwtHelper, UserDetailsService userDetailsService) {
        this.jwtHelper = jwtHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("requests="+request.getHeaderNames().toString());
        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;
        System.out.println("authorizationHeader="+authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            System.out.println("b1111111111111111");
            token = authorizationHeader.substring(7);
            System.out.println("token="+token);
            try{
                email = jwtHelper.getUsernameFromToken(token);
                System.out.println("email="+email);
            }catch (ExpiredJwtException e){
                System.out.println("ExpiredJwtException______isRefreshToken");
                String isRefreshToken = request.getHeader("isRefreshToken");
            }

        }
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if(a!=null){
            System.out.println("Authentication="+a.toString());
        }else{
            System.out.println("Authentication is null");
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("b2222222222222222");
            var userDetails = userDetailsService.loadUserByUsername(email);
            boolean isTokenValid = jwtHelper.validateToken(token);
            if (isTokenValid) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // TODO ????
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        System.out.println("b333333333333333333333333");
        filterChain.doFilter(request, response);
    }
}
