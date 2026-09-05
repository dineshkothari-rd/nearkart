package com.nearkart.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	private RoleName name;

	private Instant createdAt;
	private Instant updatedAt;

	protected Role() {
	}

	public RoleName getName() {
		return name;
	}
}
