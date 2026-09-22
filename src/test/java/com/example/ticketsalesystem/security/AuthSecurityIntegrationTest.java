package com.example.ticketsalesystem.security;

import com.example.ticketsalesystem.user.User;
import com.example.ticketsalesystem.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void predefinedUsersShouldExist(){

        User admin = userRepository.findByUsername("admin").orElseThrow();
        User user = userRepository.findByUsername("user")
                .orElseThrow();
        assertEquals("ADMIN", admin.getRole());
        assertEquals("USER", user.getRole());
    }

    @Test
    void passwordsShouldBeStoredEncoded(){

        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertNotEquals("admin123", admin.getPassword());
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()));
    }

    @Test
    void loginShouldReturnValidJwt() throws Exception {

        String token = login("user", "user123");
        assertNotNull(token);
        assertFalse(token.isBlank());
        Jwt jwt = jwtDecoder.decode(token);


        assertEquals("user", jwt.getSubject());
        assertTrue(jwt.getClaimAsStringList("roles").contains("USER"));
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
    }

    @Test
    void invalidPasswordShouldFailLogin() throws Exception {

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtShouldReturn401() throws Exception {

        mockMvc.perform(post("/events/1/tickets")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ticketPurchaseWithoutJwtShouldReturn401() throws Exception {
        mockMvc.perform(post("/events/1/tickets")).andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {

        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = Pattern
                .compile("\"token\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(response);

        assertTrue(
                matcher.find(),
                "Login response does not contain token: " + response
        );

        return matcher.group(1);
    }
}