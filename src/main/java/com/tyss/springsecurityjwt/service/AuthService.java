package com.tyss.springsecurityjwt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tyss.springsecurityjwt.dto.JwtAuthenticationResponse;
import com.tyss.springsecurityjwt.dto.LoginRequest;
import com.tyss.springsecurityjwt.dto.SignUpRequest;
import com.tyss.springsecurityjwt.exception.AppException;
import com.tyss.springsecurityjwt.exception.ConflictException;
import com.tyss.springsecurityjwt.model.Role;
import com.tyss.springsecurityjwt.model.RoleName;
import com.tyss.springsecurityjwt.model.User;
import com.tyss.springsecurityjwt.repository.RoleRepository;
import com.tyss.springsecurityjwt.repository.UserRepository;
import com.tyss.springsecurityjwt.security.JwtTokenProvider;
import com.tyss.springsecurityjwt.security.UserPrincipal;

import java.util.Collections;

@Service
@Slf4j
public class AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtTokenProvider tokenProvider;

	public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = tokenProvider.generateToken(authentication);

		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		log.info("User with [email: {}] has logged in", userPrincipal.getEmail());

		return new JwtAuthenticationResponse(jwt);
	}

	public Long registerUser(SignUpRequest signUpRequest) {

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			throw new ConflictException("Email [email: " + signUpRequest.getEmail() + "] is already taken");
		}

		User user = new User(signUpRequest.getName(), signUpRequest.getEmail(), signUpRequest.getPassword());

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
				.orElseThrow(() -> new AppException("User Role not set. Add default roles to database."));

		user.setRoles(Collections.singleton(userRole));

		log.info("Successfully registered user with [email: {}]", user.getEmail());

		return userRepository.save(user).getId();
	}
}
