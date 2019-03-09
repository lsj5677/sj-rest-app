package com.sj.ws.service;

import com.sj.ws.shared.dto.UserDto;

public interface UserService {
	UserDto createUser(UserDto user);
	UserDto getUserByUserId(String userId);
	UserDto updateUser(String userId, UserDto user);
	void deleteUser(String userId);
}
