package com.jobportal.service;

import com.jobportal.dto.*;
import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import com.jobportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {

        if (userRepository.findByEmail(signUpRequestDto.getEmail()).isPresent())
            throw new RuntimeException("Email already exists");

        User user = modelMapper.map(signUpRequestDto, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        SignUpResponseDto responseDto = modelMapper.map(savedUser, SignUpResponseDto.class);
        responseDto.setMessage("User registered successfully");

        return responseDto;
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid email or password");

        String token = jwtTokenProvider.generateToken(user);

        return new LoginResponseDto(token, user.getId(), user.getName(), user.getRole().name());
    }

    public UserResponseDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Maps User entity properties directly to our clean UserResponseDto
        return modelMapper.map(user, UserResponseDto.class);
    }
}
