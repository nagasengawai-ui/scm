package com.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailsender;
	
	public boolean sendEmail(String to,String subject,String message)
	{
	try {	SimpleMailMessage mail=new SimpleMailMessage();
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(message);
		
		mailsender.send(mail);
		return true;
	}
	catch(Exception e){
		e.printStackTrace();
		return false;
	}
		
	}
	

}
