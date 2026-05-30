package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Service
public class UserDetailServiceimpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;  // Fixed typo in variable name

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from database using email (assuming username = email)
        User user = userRepository.findByEmail(username);  // Requires method in UserRepository

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return new CustomUserDetails(user);
    }
}
