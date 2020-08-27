package com.app.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.app.domain.User;
import com.app.domain.UserPrincipal;
import com.app.enumeration.Role;
import com.app.exception.EmailExistException;
import com.app.exception.UsernameExistException;
import com.app.repository.UserRepository;

@Service
@Transactional
@Qualifier("userDetailsService") // use user own userDetailsService
public class UserServiceImpl implements UserService,UserDetailsService {

//	private Logger LOGGER= LoggerFactory.getLogger(UserServiceImpl.class);
	private Logger LOGGER= LoggerFactory.getLogger(getClass());
	
	private BCryptPasswordEncoder passwordEncoder;
	private UserRepository userRepository;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder) {
		this.userRepository  = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findUserByUsername(username);
		if(user == null)
		{
			LOGGER.error("User not found by userName : "+ username);
			throw new UsernameNotFoundException("User not found by userName : "+ username);
		}
		else
		{
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info("Returning found user by username : " + username);
			return userPrincipal;
		}		
	}

	@Override
	public User register(String firstName, String lastName, String username, String email) throws UsernameNotFoundException, EmailExistException, UsernameExistException {
		validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
		
		User user= new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		String encodedPassword = encodedPassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(true);
		user.setActive(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl());
		userRepository.save(user);
		LOGGER.info("New User password : " + password);
		return user;
	}

	private String getTemporaryProfileImageUrl() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
	}

	private String encodedPassword(String password) {
		return passwordEncoder.encode(password);
	}

	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() { 
		return RandomStringUtils.randomNumeric(10);
	}

	private User validateNewUsernameAndEmail(String currentUsername,String newUsername,String newEmail) throws EmailExistException, UsernameExistException,UsernameNotFoundException {
		
		if(StringUtils.isNotBlank(currentUsername)) 
		{
			User currentUser = findUserByUserName(currentUsername);
			if(currentUser == null)
			{
				throw new UsernameNotFoundException("No user found by name : "+ currentUsername);
			}
			
			User userByNewUsername = findUserByUserName(newUsername);
			if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) 
			{
				throw new UsernameExistException("Username already exist");
			}
			
			User userByNewEmail = findUserByUserName(newEmail);
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) 
			{
				throw new EmailExistException("Username already exist");
			}
			return currentUser;
		}
		else
		{
			User userByUsername = findUserByUserName(newUsername);
			if(userByUsername != null) 
			{
				throw new UsernameExistException("Username already exist");
			}
			
			User userByEmail = findUserByUserName(newEmail);
			if(userByEmail != null) 
			{
				throw new EmailExistException("Username already exist");
			}
			return null;
		}
	}

	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByUserName(String username) {
		return userRepository.findUserByUsername(username);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

}
