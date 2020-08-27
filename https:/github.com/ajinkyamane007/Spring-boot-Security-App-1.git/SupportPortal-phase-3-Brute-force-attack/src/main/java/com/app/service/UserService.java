package com.app.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.app.domain.User;
import com.app.exception.EmailExistException;
import com.app.exception.UsernameExistException;

public interface UserService {
	
	User register(String firstName,String lastName,String username,String email) throws UsernameNotFoundException, EmailExistException, UsernameExistException;
    
	List<User> getUsers();
	
	User findUserByUserName(String username);
	
	User findUserByEmail(String email);
	
}
