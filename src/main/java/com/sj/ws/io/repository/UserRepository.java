package com.sj.ws.io.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.sj.ws.io.entity.UserEntity;

@Repository

public interface UserRepository extends PagingAndSortingRepository<UserEntity, Long> {

	// method name should be findBy<field name> to find the data entry based on the field input.
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
	UserEntity findUserByEmailVerificationToken(String token);
}
