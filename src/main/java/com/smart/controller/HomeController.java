package com.smart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	  @Autowired
	    private BCryptPasswordEncoder passwordEncoder;
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;
    
    
    //handler for custom log in
    @GetMapping("/login")
    public String customLogin(Model model)
    {
    	
    	model.addAttribute("title","log in page");
    	return "login";
    }
    
    
    
    
    
    
    
    
    

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User()); // Add empty user object for form binding
        return "signup";
    }

    @PostMapping("/do-register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
            Model model,
            HttpSession session) {

        try {
            if (!agreement) {
                LOGGER.warn("User did not agree to terms and conditions");
                session.setAttribute("message", new Message("You must agree to the terms and conditions.", "alert alert-danger"));
                model.addAttribute("user", user);
                return "signup";
            }

            if (result.hasErrors()) {
                LOGGER.warn("Validation errors: {}", result.toString());
                model.addAttribute("user", user);
                return "signup";
            }

            if (userRepository.findByEmail(user.getEmail()) != null) {
                session.setAttribute("message", new Message("Email already registered.", "alert alert-danger"));
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("Designer.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);

            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered!", "alert alert-success"));

            return "register-success";
        } catch (Exception e) {
            LOGGER.error("Registration failed", e);
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert alert-danger"));
            return "signup";
        }
    }

        
        
        
    }


