package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository <Contact,Integer> {
	
	//pegination ....
	//currentpage
	//contact per page 
    public Page<Contact> findByUserId(int userId,Pageable pageable);
    void deleteById(int id);
    
    
    //search
    
    public List<Contact> findByNameContainingAndUser (String name,User user);

}
