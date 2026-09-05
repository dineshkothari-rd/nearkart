package com.nearkart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nearkart.auth.RefreshTokenRepository;
import com.nearkart.user.RoleRepository;
import com.nearkart.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
		"nearkart.auth.jwt-secret=test-secret-with-at-least-32-bytes"
})
class NearKartApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private UserRepository users;
	@MockitoBean
	private RoleRepository roles;
	@MockitoBean
	private RefreshTokenRepository refreshTokens;

	@Test
	void healthIsPublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk());
	}

}
