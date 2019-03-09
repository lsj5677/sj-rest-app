package com.sj.ws.io.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sj.ws.io.entity.UserEntity;

@Repository

public interface UserRepository extends CrudRepository<UserEntity, Long> {

	// method name should be findBy<field name> to find the data entry based on the field input.
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
}
