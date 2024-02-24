package com.project.user;


import com.project.configuration.security.JwtToken;
import com.project.user.entity.User;
import com.project.user.entity.dto.SignInDto;
import com.project.user.entity.dto.SignUpDto;
import com.project.user.repository.UserRepository;
import com.project.user.service.CheckReference;
import com.project.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CheckReference checkReference;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtToken jwtToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입")
    void signUpTest() {
        //given
        SignUpDto sign = SignUpDto.builder()
                .email("test@naver.com")
                .nickname("nickname")
                .password("password1")
                .build();
        when(userRepository.existsByEmail(sign.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(sign.getNickname())).thenReturn(false);

        //when
        String result = userService.signUp(sign);

        //then
        assertEquals(result, sign.getEmail() + "님의 회원가입 완료");
    }

    @Test
    @DisplayName("로그인")
    void signInTest() {
        //given
        SignInDto sign = SignInDto.builder()
                .email("test@naver.com")
                .password("password1")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .password(passwordEncoder.encode("password1"))
                .build();

        when(userRepository.findByEmail(sign.getEmail())).thenReturn(Optional.of(user));
        when(jwtToken.generateAccessToken(user.getEmail())).thenReturn("token");

        String token = userService.signIn(sign);

        assertEquals("token", token);
    }
}