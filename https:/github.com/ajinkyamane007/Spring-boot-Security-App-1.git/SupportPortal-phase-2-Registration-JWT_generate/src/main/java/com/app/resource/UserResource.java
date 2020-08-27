package com.app.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.SecurityConstant;
import com.app.domain.User;
import com.app.domain.UserPrincipal;
import com.app.exception.EmailExistException;
import com.app.exception.ExceptionHandling;
import com.app.exception.UserNotFoundException;
import com.app.exception.UsernameExistException;
import com.app.service.UserService;
import com.app.utility.JWTTokenProvider;

@RestController
@RequestMapping(path  = { "/", "/user" })
public class UserResource extends ExceptionHandling{
	
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JWTTokenProvider jWTTokenProvider;
	
	@Autowired
	public UserResource(UserService userService,AuthenticationManager authenticationManager,JWTTokenProvider jWTTokenProvider) {
		this.userService = userService;
		this.authenticationManager=authenticationManager;
		this.jWTTokenProvider=jWTTokenProvider;
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) 
	{   
		authenticate(user.getUsername(),user.getPassword());  // wrong -username,password,accountLocked,acountDiabled
		User loginUser = userService.findUserByUserName(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<User>(loginUser, jwtHeader, HttpStatus.OK); // body, headers, status
	}

	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {

		HttpHeaders headers = new HttpHeaders();
		headers.add(SecurityConstant.JWT_TOKEN_HEADER, jWTTokenProvider.generateJwtToken(userPrincipal)); //headerName, headerValue
		return headers;
	}

	private void authenticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));	// principal, credentials
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameNotFoundException, EmailExistException, UsernameExistException 
	{
		User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK); //body, status
	}

}

  // 2hhtFXfGFp

//@GetMapping("/home")
//public String showUser() throws UserNotFoundException  //EmailExistException
//{
//	
////	return "Application working very well";
////	throw new EmailExistException("This email address allready taken");
//	throw new UserNotFoundException("The user was not found");
//}