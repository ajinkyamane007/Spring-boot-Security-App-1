package com.app.listener;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.app.service.LoginAttemptService;

@Component
public class AuthenticationFailureListener {
	
	private LoginAttemptService loginAttemptService;

	@Autowired
	public AuthenticationFailureListener(LoginAttemptService loginAttemptService) {
		this.loginAttemptService = loginAttemptService;
	}
	
	@EventListener    // user - Fail to Authenticate Added into cache
	 public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) // throws ExecutionException 
	{
		 Object principal = event.getAuthentication().getPrincipal();
		 if( principal instanceof String) {
			 String username = (String) event.getAuthentication().getPrincipal();
			 loginAttemptService.addUserToLoginAttemptCache(username);
		 }
	 }

}
