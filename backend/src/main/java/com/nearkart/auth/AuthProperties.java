package com.nearkart.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nearkart.auth")
public record AuthProperties(String jwtSecret, Duration accessTtl, Duration refreshTtl,
	boolean secureCookie, String cookieSameSite) {
}
