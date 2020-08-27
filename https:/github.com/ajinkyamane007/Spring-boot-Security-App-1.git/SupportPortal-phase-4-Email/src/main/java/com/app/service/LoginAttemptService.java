package com.app.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	
	 private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
	 private static final int ATTEMPT_INCREMENT = 1;
	 
	 // Initialization of cache 
	 private LoadingCache<String, Integer>loginAttemptCache;
	 
	 public LoginAttemptService() {
		 super();
		 loginAttemptCache = CacheBuilder.newBuilder()
				                         .expireAfterWrite(10, TimeUnit.MINUTES)
				                         .maximumSize(100)
				                         .build(new CacheLoader<String, Integer>(){
				                                public Integer load(String key) 
				                                    {  return 0;  }
				                                });
	 }
	 // if username - authenticated then remove their entry from DB
	 public void evictUserFromLoginAttemptCache(String username) {
		 loginAttemptCache.invalidate(username);
	 }
	 
	 // Add user in DB for every wrong - Authentication
	 public void addUserToLoginAttemptCache(String username) // throws ExecutionException 
	 {
		    int attempts = 0;
	        try 
	        {
	        	attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
	        	loginAttemptCache.put(username, attempts);
			}
	        catch (ExecutionException e)
	        {
	        	e.printStackTrace();
			}
		 
//		    int attempts = 0;
//        	attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
//        	loginAttemptCache.put(username, attempts);
	 }
	 
	 // Check  wrong authentication attemtes should not exceeds 5 time 
	 public boolean hasExceededMaxAttempts(String username) // throws ExecutionException 
	 {
		try 
		{
			return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
		} 
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;		 
	 }
	    
}
