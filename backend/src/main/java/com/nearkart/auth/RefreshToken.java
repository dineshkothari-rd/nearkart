package com.nearkart.auth;

import java.time.Instant;
import java.util.UUID;

import com.nearkart.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "refresh_tokens")
class RefreshToken {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	private UUID familyId;
	@Column(length = 64)
	private String tokenHash;
	@Column(length = 64)
	private String replacedByHash;
	private Instant expiresAt;
	private Instant revokedAt;
	private Instant createdAt;
	private Instant updatedAt;

	protected RefreshToken() {
	}

	RefreshToken(User user, UUID familyId, String tokenHash, Instant expiresAt, Instant now) {
		this.id = UUID.randomUUID();
		this.user = user;
		this.familyId = familyId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.createdAt = now;
		this.updatedAt = now;
	}

	void replaceWith(String replacementHash, Instant now) {
		this.replacedByHash = replacementHash;
		this.revokedAt = now;
		this.updatedAt = now;
	}

	void revoke(Instant now) {
		this.revokedAt = now;
		this.updatedAt = now;
	}

	User user() {
		return user;
	}

	UUID familyId() {
		return familyId;
	}

	Instant expiresAt() {
		return expiresAt;
	}

	boolean revoked() {
		return revokedAt != null;
	}
}
