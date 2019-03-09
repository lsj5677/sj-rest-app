package com.sj.ws.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sj.ws.io.entity.UserEntity;
import com.sj.ws.io.repository.UserRepository;
import com.sj.ws.service.UserService;
import com.sj.ws.shared.Utils;
import com.sj.ws.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;

	@Override
	public UserDto createUser(UserDto userDto) {
		
		if (userRepository.findByEmail(userDto.getEmail()) != null) {
			throw new RuntimeException("Record already exists!");
		}
		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(userDto, userEntity); // fields should match each other

		String publicUserID = utils.generateUserId(30);
		userEntity.setUserId(publicUserID);
		userEntity.setEncryptedPassword(userDto.getPassword());

		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(storedUserDetails, returnValue);

		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userDetails = userRepository.findByUserId(userId);
		
		if (userDetails == null) {
			throw new RuntimeException("User not found!");
		}
		
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userDetails, returnValue);
		
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {
		UserEntity userDetails = userRepository.findByUserId(userId);
		
		if (userDetails == null) {
			throw new RuntimeException("User not found!");
		}
		
		// This step is to update user entity object, not data persistence.
		userDetails.setFirstName(user.getFirstName());
		userDetails.setLastName(user.getLastName());
		
		// UserRepository sholud be called to Persist the updated object to my DATABASE.
		UserEntity updatedUserDetails = userRepository.save(userDetails);
		
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		
		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userDetails = userRepository.findByUserId(userId);
		
		if (userDetails == null) {
			throw new RuntimeException("User not found!");
		}
		
		userRepository.delete(userDetails);
		
	}

}
