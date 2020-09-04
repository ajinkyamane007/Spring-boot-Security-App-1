package com.app.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.app.constant.SecurityConstant;
import com.app.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAccessDeniedHandler  implements AccessDeniedHandler{
    
	// User Try to access Resource if he don't have enough Permission to it - This method triggered
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,AccessDeniedException accessDeniedException) throws IOException, ServletException {

		// UNAUTHORIZED(401, "Unauthorized")
		HttpResponse httpresponse = new HttpResponse(
				HttpStatus.UNAUTHORIZED.value(),    // 401
				HttpStatus.UNAUTHORIZED,            // UNAUTHORIZED
				HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase(), // Unauthorized -> UNAUTHORIZED
				SecurityConstant.ACCESS_DENIED_MESSAGE);   // You do not have permission to access this page
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); // APPLICATION_JSON_VALUE = "application/json";
		response.setStatus(HttpStatus.UNAUTHORIZED.value() );
		
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper mapper=new ObjectMapper();
		mapper.writeValue(outputStream, httpresponse);
		outputStream.flush();			
	}

}
