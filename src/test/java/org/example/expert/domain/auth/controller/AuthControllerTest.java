package org.example.expert.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 테스트")
    public void signup_Success() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password", "USER");
        SignupResponse response = new SignupResponse("bearer_token");

        given(authService.signup(any(SignupRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("bearer_token"));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void signin_Success() throws Exception {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "password");
        SigninResponse response = new SigninResponse("bearer_token");

        given(authService.signin(any(SigninRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("bearer_token"));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 잘못된 요청")
    public void signup_Failure_InvalidRequest() throws Exception {
        // given
        SignupRequest request = new SignupRequest("", "", "");

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 요청")
    public void signin_Failure_InvalidRequest() throws Exception {
        // given
        SigninRequest request = new SigninRequest("", "");

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}