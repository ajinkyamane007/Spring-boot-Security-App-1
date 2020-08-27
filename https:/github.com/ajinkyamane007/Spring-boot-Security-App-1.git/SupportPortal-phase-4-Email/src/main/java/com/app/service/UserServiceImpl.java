package com.app.service;

import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
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

import static com.app.constant.UserImplConstant.*;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService,UserDetailsService {

	//	private Logger LOGGER= LoggerFactory.getLogger(UserServiceImpl.class);
	private Logger LOGGER= LoggerFactory.getLogger(getClass());
	
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;
    private EmailService emailService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository,
			               BCryptPasswordEncoder passwordEncoder,
		                   LoginAttemptService loginAttemptService,
		                   EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findUserByUsername(username);
		if(user == null)
		{
			LOGGER.error(USER_NOT_FOUND_BY_USER_NAME + username);
			throw new UsernameNotFoundException(USER_NOT_FOUND_BY_USER_NAME + username);
		}
		else
		{
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info(RETURNING_FOUND_USER_BY_USERNAME + username);
			return userPrincipal;
		}		
	}

	private void validateLoginAttempt(User user) {
        
		if(user.isNotLocked())
		{
			if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
			   user.setNotLocked(false);	
			} 
			else {
				user.setNotLocked(true);			}
		}
		else 
		{
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}		
	}

	@Override
	public User register(String firstName, String lastName, String username, String email) throws UsernameNotFoundException, EmailExistException, UsernameExistException, MessagingException {
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
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl());
		userRepository.save(user);
		LOGGER.info("New User password : " + password);
		emailService.sendNewPasswordEmail(firstName, password, email);
		return user;
	}

	private String getTemporaryProfileImageUrl() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH).toUriString();
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
		
		User userByNewUsername = findUserByUserName(newUsername);
		User userByNewEmail    = findUserByEmail(newEmail); 

		if(StringUtils.isNotBlank(currentUsername)) 
		{
			User currentUser = findUserByUserName(currentUsername);
			if(currentUser == null)
			{
				throw new UsernameNotFoundException(USER_NOT_FOUND_BY_USER_NAME+ currentUsername);
			}
			
			if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) 
			{
				throw new UsernameExistException(USERNAME_ALREADY_EXIST);
			}
			
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) 
			{ 
				throw new EmailExistException(EMAIL_ALREADY_EXIST);
			}
			return currentUser;
		}
		else
		{
			if(userByNewUsername != null) 
			{
				throw new UsernameExistException(USERNAME_ALREADY_EXIST);
			}
			
			if(userByNewEmail != null) 
			{
				throw new EmailExistException(EMAIL_ALREADY_EXIST);
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
