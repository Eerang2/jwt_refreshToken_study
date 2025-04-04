package jwt_study.user.controller;
import jakarta.servlet.http.Cookie;
import jwt_study.user.JwtAuthenticationFilter;
import jwt_study.user.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jwt_study.user.service.UserService;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.JwtUtil;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Import(JwtAuthInterceptorTest.TestFilterConfig.class)  // ⬅️ 필터 설정 import
class JwtAuthInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    private final String expiredAccessToken = "expiredAccessToken";
    private final String validRefreshToken = "validRefreshToken";
    private final String newAccessToken = "newAccessToken";

    @Test
    @DisplayName("만료된 액세스 토큰으로 새 액세스 토큰 발급")
    void testRefreshAccessToken() throws Exception {
        // given
        TokenRes tokenRes = TokenRes.of(newAccessToken, validRefreshToken);

        // when
        when(userService.reissueAccessToken(validRefreshToken)).thenReturn(tokenRes);

        // then
        mockMvc.perform(post("/api/refresh")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                        .cookie(new Cookie("refreshToken", validRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer " + newAccessToken));
    }

    @TestConfiguration
    static class TestFilterConfig {

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private UserService userService;

        @Autowired
        private CookieUtil cookieUtil;

        @Bean
        public JwtAuthInterceptor jwtAuthInterceptor() {
            return new JwtAuthInterceptor(jwtUtil, userService, cookieUtil);
        }

        @Bean
        public WebMvcConfigurer webMvcConfigurer(JwtAuthInterceptor jwtAuthInterceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(jwtAuthInterceptor).addPathPatterns("/api/**");
                }
            };
        }
    }
}