package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    // no custom save() method!
	
	@Query("select u from User u where u.email=:email")
	public User getUserByUserName(@Param("email")String email);
	
	User findByEmail(String email);// method to fetch email
	default User saveUserCustom(User user) {
        // Add custom logic here if needed
        return save(user);
    }
}
