package com.app.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.domain.User;
import com.app.exception.EmailExistException;
import com.app.exception.ExceptionHandling;
import com.app.exception.UserNotFoundException;
import com.app.exception.UsernameExistException;
import com.app.service.UserService;

@RestController
@RequestMapping(path  = { "/", "/user" })
public class UserResource extends ExceptionHandling{
	
	private UserService userService;
	
	@Autowired
	public UserResource(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameNotFoundException, EmailExistException, UsernameExistException 
	{
		User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK); //body, status
	}
//	@GetMapping("/home")
//	public String showUser() throws UserNotFoundException  //EmailExistException
//	{
//		
////		return "Application working very well";
////		throw new EmailExistException("This email address allready taken");
//		throw new UserNotFoundException("The user was not found");
//	}

}
