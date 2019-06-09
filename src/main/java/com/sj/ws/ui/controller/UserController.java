package com.sj.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sj.ws.service.UserService;
import com.sj.ws.shared.dto.UserDto;
import com.sj.ws.ui.model.request.PasswordResetModel;
import com.sj.ws.ui.model.request.PasswordResetRequestModel;
import com.sj.ws.ui.model.request.UserDetailsRequestModel;
import com.sj.ws.ui.model.response.OperationStatusModel;
import com.sj.ws.ui.model.response.RequestOperationStatus;
import com.sj.ws.ui.model.response.UserResponseModel;

@RestController // register this class as RestContoller and receive HTTP Request
@RequestMapping("/users") // http://localhost:8080/users
public class UserController {

	@Autowired
	UserService userService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserResponseModel getUser(@PathVariable String id) {
		UserResponseModel returnValue = new UserResponseModel();

		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);

		return returnValue;
	}

	@PostMapping(
		consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, 
		produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserResponseModel createUser(@RequestBody UserDetailsRequestModel userDetails) {

		UserResponseModel returnValue = new UserResponseModel();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto createdUser = userService.createUser(userDto);
		BeanUtils.copyProperties(createdUser, returnValue);

		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserResponseModel updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {

		UserResponseModel returnValue = new UserResponseModel();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;

	}

	// delete method
	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();

		userService.deleteUser(id);

		returnValue.setOperationName(RequestOperationName.DELETE.name());
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return returnValue;
	}

	// only when request does not contain value, defaultValue is applied.
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserResponseModel> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit) {
		List<UserResponseModel> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserResponseModel userModel = new UserResponseModel();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}

		return returnValue;
	}

	/*
	 * http://localhost:8080/users/email-verification?token.=sdfsdf
	 */
	@GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {

		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		
		boolean isVerified = userService.verifyEmailToken(token);
		if (isVerified) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}

		return returnValue;
	}
	

/*
    * http://localhost:8080/users/password-reset-request
    * */
   @PostMapping(path = "/password-reset-request", 
           produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
           consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
   )
   public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
   	OperationStatusModel returnValue = new OperationStatusModel();

       boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
       
       returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
       returnValue.setOperationResult(RequestOperationStatus.ERROR.name()); // default

       if(operationResult)
       {
           returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
       }
       
       return returnValue;
   }

   @PostMapping(path = "/password-reset",
           consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
   )
   public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
   	OperationStatusModel returnValue = new OperationStatusModel();

       boolean operationResult = userService.resetPassword(
               passwordResetModel.getToken(),
               passwordResetModel.getPassword());
       
       returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
       returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

       if(operationResult)
       {
           returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
       }

       return returnValue;
   }
}
