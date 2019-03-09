package com.sj.ws.ui.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sj.ws.service.UserService;
import com.sj.ws.shared.dto.UserDto;
import com.sj.ws.ui.model.request.UserDetailsRequestModel;
import com.sj.ws.ui.model.response.OperationStatusModel;
import com.sj.ws.ui.model.response.RequestOperationStatus;
import com.sj.ws.ui.model.response.UserResponseModel;

@RestController // register this class as RestContoller and receive HTTP Request
@RequestMapping("/users") // http://localhost:8080/users
public class UserController {
	
	@Autowired
	UserService userService;
	
	@GetMapping(
        path = "/{id}"
    )
    public UserResponseModel getUser(@PathVariable String id) {
		UserResponseModel returnValue = new UserResponseModel();
        
        UserDto userDto = userService.getUserByUserId(id);
        BeanUtils.copyProperties(userDto, returnValue);
        
        return returnValue;
    }
	
	@PostMapping
	public UserResponseModel createUser(@RequestBody UserDetailsRequestModel userDetails) {

		UserResponseModel returnValue = new UserResponseModel();
	
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
	
		UserDto createdUser = userService.createUser(userDto);
		BeanUtils.copyProperties(createdUser, returnValue);
	
		return returnValue;
	}
	
	@PutMapping (
		path = "/{id}"
	)
	public UserResponseModel updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails ) {
		
		UserResponseModel returnValue = new UserResponseModel();
		
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
		
		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);
		
		return returnValue;
		
	}

	//delete method
	@DeleteMapping (
		path = "/{id}"
	)
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		
		userService.deleteUser(id);
		
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		
		return returnValue;
	}
}
