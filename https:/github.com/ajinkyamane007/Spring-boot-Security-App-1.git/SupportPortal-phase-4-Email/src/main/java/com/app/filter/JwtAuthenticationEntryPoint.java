package com.app.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.app.constant.SecurityConstant;
import com.app.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint  {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,AuthenticationException exception) throws IOException {
		
		// User own created HttpResponse
		HttpResponse httpresponse = new HttpResponse(
				HttpStatus.FORBIDDEN.value(),    // 403
				HttpStatus.FORBIDDEN,            // "Forbidden"
				HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(), // 403, "Forbidden"
				SecurityConstant.FORBIDDEN_MESSAGE);   // You need to log in to access this page
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); // "application/json"
		response.setStatus(HttpStatus.FORBIDDEN.value() ); // 403
		
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper mapper=new ObjectMapper();
		mapper.writeValue(outputStream, httpresponse);
		outputStream.flush();		
	}
}
