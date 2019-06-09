package com.sj.ws.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.sj.ws.shared.dto.UserDto;

public interface UserService extends UserDetailsService{
	UserDto createUser(UserDto user);
	UserDto getUserByUserId(String userId);
	UserDto getUser(String email);
	UserDto updateUser(String userId, UserDto user);
	void deleteUser(String userId);
	List<UserDto> getUsers(int page, int limit);
	boolean verifyEmailToken(String token);
	boolean requestPasswordReset (String email);
	boolean resetPassword(String token, String password);
	
}
