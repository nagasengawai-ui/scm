package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "normal/forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String otpForm(@RequestParam("email") String email,HttpSession session)
	{
		//gernate otp
		System.out.println("email="+email);

		Random random=new Random();
		
		int otp=random.nextInt(999999);
		
		System.out.println("OTP="+otp);
		
		String subject="Password reset OTP";
		String message="Your OTP is :"+otp;
		
		
		boolean flag=this.emailService.sendEmail(email, subject, message);
		if(flag)
		{
			//save otp and email in session
			session.setAttribute("myOtp", otp);
			session.setAttribute("email", email);
			
			return "normal/verify_otp";

		}else {
			return "normal/forgot_email_form";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {

	    // Session se OTP aur email lo
	    Integer myOtp = (Integer) session.getAttribute("myOtp");
	    String email = (String) session.getAttribute("email");

	    if (myOtp == null || email == null) {
	        // Session expire ya koi error
	        return "normal/forgot_email_form";
	    }

	    if (myOtp == otp) {
	        // OTP correct → check user in database
	        User user = this.userRepository.getUserByUserName(email);

	        if (user == null) {
	            // User not found
	            // TODO: show error message
	            return "normal/verify_otp";
	        } else {
	            // OTP correct & user exists → load change password page
	            return "normal/change_password_form";
	        }
	    } else {
	        // OTP incorrect → wapas verify page
	        // TODO: show "Incorrect OTP" message
	        return "normal/verify_otp";
	    }
	}

	
	@RequestMapping("/change-password")
	public String openChangePasswordForm() {
	    return "normal/change_password_form"; // ye template ko return karega
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,
	                             @RequestParam("confirmPassword") String confirmPassword,
	                             HttpSession session) {
	    
	    String email = (String) session.getAttribute("email"); // email session se le rahe
	    User user=this.userRepository.getUserByUserName(email);
	    user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
	    this.userRepository.save(user);
	    
	    if(!newPassword.equals(confirmPassword)) {
	        return "normal/change_password_form"; // passwords match nahi → wapas form
	    }

	    
	    // TODO: database me password update karo
	    System.out.println("Password for " + email + " changed to: " + newPassword);

	    return "login"; // password change hone ke baad login page
	}



}
