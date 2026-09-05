package com.nearkart.auth;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nearkart.audit.AuditLog;
import com.nearkart.audit.AuditLogRepository;
import com.nearkart.user.RoleName;
import com.nearkart.user.RoleRepository;
import com.nearkart.user.User;
import com.nearkart.user.UserRepository;

@Component
class AdminBootstrap implements ApplicationRunner {
	private final UserRepository users;
	private final RoleRepository roles;
	private final AuditLogRepository auditLogs;
	private final PasswordEncoder passwords;
	private final Clock clock;
	private final String email;
	private final String password;

	AdminBootstrap(UserRepository users, RoleRepository roles, AuditLogRepository auditLogs,
			PasswordEncoder passwords, Clock clock,
			@Value("${nearkart.bootstrap-admin.email:}") String email,
			@Value("${nearkart.bootstrap-admin.password:}") String password) {
		this.users = users;
		this.roles = roles;
		this.auditLogs = auditLogs;
		this.passwords = passwords;
		this.clock = clock;
		this.email = email.trim().toLowerCase(Locale.ROOT);
		this.password = password;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments arguments) {
		if (email.isEmpty() && password.isEmpty()) return;
		if (!email.contains("@") || password.length() < 12 || password.length() > 72)
			throw new IllegalStateException("ADMIN_EMAIL and a 12-72 character ADMIN_PASSWORD must both be valid");

		var role = roles.findByName(RoleName.ADMIN).orElseThrow();
		var now = clock.instant();
		User admin = users.findByEmail(email).orElse(null);
		boolean changed;
		if (admin == null) {
			admin = users.save(new User(email, passwords.encode(password), "NearKart Admin", role, now));
			changed = true;
		} else {
			changed = admin.addRole(role);
			if (changed) users.save(admin);
		}
		if (changed) auditLogs.save(new AuditLog(admin, "ADMIN_BOOTSTRAPPED", "USER", admin.getId(), Map.of(), now));
	}
}
