package com.sj.ws.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sj.ws.SpringApplicationContext;
import com.sj.ws.service.UserService;
import com.sj.ws.shared.dto.UserDto;
import com.sj.ws.ui.model.request.UserLoginRequestModel;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;

	// Authenticaton manager is initialized in the constructor
	public AuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	// this method will be automatically triggered to authenticate user
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		try {
			// Json payload use UserLoginRequestModel to be read
			UserLoginRequestModel creds = new ObjectMapper().readValue(req.getInputStream(),
					UserLoginRequestModel.class);

			// it will look up DB using loadUserByUsername as we set UserDetailsService to
			// AuthenticationManagerBuilder in WebSecurity class
			// and try authenticate user by given email and password from creds
			// and if email and password match the record we have
			// then successfulAuthentication method is triggered.
			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getPassword(), new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// once above authentication is done successfully, this method will be called
	// automatically
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		// userName is read from authentication object
		String userName = ((User) auth.getPrincipal()).getUsername();

		String token = Jwts.builder().setSubject(userName)
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret()).compact();

		UserService userService = (UserService) SpringApplicationContext.getBean("userServiceImpl");
		UserDto userDto = userService.getUser(userName);

		// once token is generated, it will be included into header info
		// and client who r0eceives this response will need to extract the token and
		// store it
		// in order for client to communicate with our API, this token should be
		// included into request header
		// otherwise, the request won't be autherozed.
		res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

		res.addHeader("UserID", userDto.getUserId());

	}
}
