package com.nearkart.user;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.auth.AuthModels.UserView;
import com.nearkart.common.ApiException;
import com.nearkart.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/me")
class UserController {

	private final UserRepository users;

	UserController(UserRepository users) {
		this.users = users;
	}

	@GetMapping
	ApiResponse<UserView> me(@AuthenticationPrincipal Jwt jwt) {
		User user = users.findById(UUID.fromString(jwt.getSubject()))
			.filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "User not found"));
		return ApiResponse.success(new UserView(user.getId(), user.getEmail(), user.getDisplayName(),
			user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet())));
	}
}
