package com.FST.GestionDesVentes.Services;

import com.FST.GestionDesVentes.Entities.AuthenticationRequest;
import com.FST.GestionDesVentes.Entities.AuthenticationResponse;
import com.FST.GestionDesVentes.Entities.RegisterRequest;
import com.FST.GestionDesVentes.Entities.Token;
import com.FST.GestionDesVentes.Entities.TokenType;
import com.FST.GestionDesVentes.Entities.User;
import com.FST.GestionDesVentes.Repositories.TokenRepository;
import com.FST.GestionDesVentes.Repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthentificationService {

	 private final UserRepository repository;
	    private final TokenRepository tokenRepository;
	    private final PasswordEncoder passwordEncoder;
	    private final JwtService jwtService;
	    private final AuthenticationManager authenticationManager;

	    @Autowired
	    public AuthentificationService(
	            UserRepository repository,
	            TokenRepository tokenRepository,
	            PasswordEncoder passwordEncoder,
	            JwtService jwtService,
	            AuthenticationManager authenticationManager
	    ) {
	        this.repository = repository;
	        this.tokenRepository = tokenRepository;
	        this.passwordEncoder = passwordEncoder;
	        this.jwtService = jwtService;
	        this.authenticationManager = authenticationManager;
	    }
	    
	    public AuthenticationResponse register(RegisterRequest request) {
	        var user = new User(
	            request.getFirstname(),
	            request.getLastname(),
	            request.getEmail(),
	            passwordEncoder.encode(request.getPassword()),
	            request.getRole() // Utilisation du constructeur mis à jour
	        );
	        var savedUser = repository.save(user);
	        var jwtToken = jwtService.generateToken(savedUser);
	        var refreshToken = jwtService.generateRefreshToken(savedUser);
	        saveUserToken(savedUser, jwtToken);
	        return new AuthenticationResponse(jwtToken, refreshToken);
	    }

	    public AuthenticationResponse authenticate(AuthenticationRequest request) {
	        authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(
	                request.getEmail(),
	                request.getPassword()
	            )
	        );
	        var user = repository.findByEmail(request.getEmail())
	            .orElseThrow();
	        var jwtToken = jwtService.generateToken(user);
	        var refreshToken = jwtService.generateRefreshToken(user);
	        revokeAllUserTokens(user);
	        saveUserToken(user, jwtToken);
	        return new AuthenticationResponse(jwtToken, refreshToken);
	    }

	    private void saveUserToken(User user, String jwtToken) {
	        var token = new Token(
	            jwtToken,
	            TokenType.BEARER,
	            false,
	            false,
	            user
	        );
	        tokenRepository.save(token);
	    }

	    private void revokeAllUserTokens(User user) {
	        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
	        if (validUserTokens.isEmpty()) return;
	        validUserTokens.forEach(token -> {
	            token.setExpired(true);
	            token.setRevoked(true);
	        });
	        tokenRepository.saveAll(validUserTokens);
	    }

	    public void refreshToken(
	            HttpServletRequest request,
	            HttpServletResponse response
	    ) throws IOException {
	        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
	        final String refreshToken;
	        final String userEmail;
	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            return;
	        }
	        refreshToken = authHeader.substring(7);
	        userEmail = jwtService.extractUsername(refreshToken);
	        if (userEmail != null) {
	            var user = this.repository.findByEmail(userEmail)
	                    .orElseThrow();
	            if (jwtService.isTokenValid(refreshToken, user)) {
	                var accessToken = jwtService.generateToken(user);
	                revokeAllUserTokens(user);
	                saveUserToken(user, accessToken);
	                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
	                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
	            }
	        }
	    }
}
