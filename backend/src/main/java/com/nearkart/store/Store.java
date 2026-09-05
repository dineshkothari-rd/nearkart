package com.nearkart.store;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nearkart.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "stores")
public class Store {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private User owner;

	private String name;
	private String slug;
	private String description;
	private String phone;
	private String timezone;

	@Enumerated(EnumType.STRING)
	private StoreStatus status;

	private String approvalReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by")
	private User approvedBy;

	private Instant approvedAt;

	@Version
	private int version;

	@OneToOne(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private StoreLocation location;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoreHour> hours = new ArrayList<>();

	private Instant createdAt;
	private Instant updatedAt;

	protected Store() {
	}

	Store(User owner, StoreModels.CreateStoreRequest request, String slug, Instant now) {
		this.id = UUID.randomUUID();
		this.owner = owner;
		this.slug = slug;
		this.status = StoreStatus.PENDING;
		this.createdAt = now;
		update(request.name(), request.description(), request.phone(), request.timezone(), request.location(), now);
	}

	void update(String name, String description, String phone, String timezone,
			StoreModels.LocationInput locationInput, Instant now) {
		if (name != null) this.name = name.trim();
		if (description != null) this.description = description.trim();
		if (phone != null) this.phone = phone.trim();
		if (timezone != null) this.timezone = timezone.trim();
		if (locationInput != null) {
			if (location == null) location = new StoreLocation(this, locationInput, now);
			else location.update(locationInput, now);
		}
		this.updatedAt = now;
	}

	void replaceHours(List<StoreModels.HourInput> inputs, Instant now) {
		hours.clear();
		inputs.forEach(input -> hours.add(new StoreHour(this, input, now)));
		updatedAt = now;
	}

	void decide(User admin, StoreModels.ApprovalDecision decision, String reason, Instant now) {
		status = decision == StoreModels.ApprovalDecision.APPROVE ? StoreStatus.APPROVED : StoreStatus.REJECTED;
		approvalReason = reason == null ? null : reason.trim();
		approvedBy = admin;
		approvedAt = now;
		updatedAt = now;
	}

	UUID id() { return id; }
	UUID ownerId() { return owner.getId(); }
	String name() { return name; }
	String slug() { return slug; }
	String description() { return description; }
	String phone() { return phone; }
	String timezone() { return timezone; }
	StoreStatus status() { return status; }
	String approvalReason() { return approvalReason; }
	int version() { return version; }
	StoreLocation location() { return location; }
	List<StoreHour> hours() { return List.copyOf(hours); }
	Instant createdAt() { return createdAt; }
	Instant updatedAt() { return updatedAt; }
}
