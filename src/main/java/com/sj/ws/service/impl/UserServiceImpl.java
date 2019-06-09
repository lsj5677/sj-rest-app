package com.sj.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sj.ws.io.entity.PasswordResetTokenEntity;
import com.sj.ws.io.entity.UserEntity;
import com.sj.ws.io.repository.PasswordResetTokenRepository;
import com.sj.ws.io.repository.UserRepository;
import com.sj.ws.service.UserService;
import com.sj.ws.shared.AmazonSES;
import com.sj.ws.shared.Utils;
import com.sj.ws.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto userDto) {

		if (userRepository.findByEmail(userDto.getEmail()) != null) {
			throw new RuntimeException("Record already exists!");
		}
		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(userDto, userEntity); // fields should match each other

		String publicUserID = utils.generateUserId(30);
		userEntity.setUserId(publicUserID);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserID));
		userEntity.setEmailVerificationStatus(false);

		// This is the actual step to persiste data into database
		// if Data persistence is successfully completed, it will return the saved data.
		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(storedUserDetails, returnValue);
		
		//Send an email message to user to verify their email address
		new AmazonSES().verifyEmail(returnValue);

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

	// this method is automatically getting called whenever a user is attempting to login
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		// if it is not null, it returns user object which is spring object and
		// implements userDetails
		//return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), userEntity.getEmailVerificationStatus(),
				true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> returnValue = new ArrayList<>();

		if (page > 0)
			page = page - 1;

		Pageable pageableRequest = PageRequest.of(page, limit);

		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();

		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}

		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		boolean returnValue = false;

		// Find user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);

		if (userEntity != null) {
			boolean hastokenExpired = Utils.hasTokenExpired(token);
			if (!hastokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}

		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean returnValue = false;
		
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if (userEntity == null) {
			return returnValue;
		}
		
		// Contain userId as payload
		String token = new Utils().generatePasswordResetToken(userEntity.getUserId());
		
		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);
		
		returnValue = new AmazonSES().sendPasswordResetRequest(
				userEntity.getFirstName(),
				userEntity.getEmail(),
				token);
				
		return returnValue;
	}
	
	@Override
	public boolean resetPassword(String token, String password) {
	       boolean returnValue = false;
	       
	       if( Utils.hasTokenExpired(token) )
	       {
	           return returnValue;
	       }
	       
	       PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

	       if (passwordResetTokenEntity == null) {
	           return returnValue;
	       }

	       // Prepare new password
	       String encodedPassword = bCryptPasswordEncoder.encode(password);
	       
	       // Update User password in database
	       UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
	       userEntity.setEncryptedPassword(encodedPassword);
	       UserEntity savedUserEntity = userRepository.save(userEntity);

	       // Verify if password was saved successfully
	       if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
	           returnValue = true;
	       }
	  
	       // Remove Password Reset token from database
	       passwordResetTokenRepository.delete(passwordResetTokenEntity);
	       
	       return returnValue;
	}

}
