package com.FST.GestionDesVentes.Services;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.FST.GestionDesVentes.Entities.ChangePasswordRequest;
import com.FST.GestionDesVentes.Entities.User;
import com.FST.GestionDesVentes.Repositories.UserRepository;

import java.security.Principal;

@Service
@RequiredArgsConstructor 
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository ;
    
    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

  
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

       
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

       
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        
        repository.save(user);
    }
}