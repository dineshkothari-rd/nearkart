package com.nearkart.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nearkart.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actor_id")
	private User actor;

	private String action;
	private String targetType;
	private UUID targetId;

	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> metadata;

	private Instant createdAt;
	private Instant updatedAt;

	protected AuditLog() {
	}

	public AuditLog(User actor, String action, String targetType, UUID targetId,
			Map<String, Object> metadata, Instant now) {
		this.id = UUID.randomUUID();
		this.actor = actor;
		this.action = action;
		this.targetType = targetType;
		this.targetId = targetId;
		this.metadata = Map.copyOf(metadata);
		this.createdAt = now;
		this.updatedAt = now;
	}
}
