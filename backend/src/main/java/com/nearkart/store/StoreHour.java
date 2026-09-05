package com.nearkart.store;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_hours")
class StoreHour {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id")
	private Store store;

	private short weekday;
	private LocalTime opensAt;
	private LocalTime closesAt;
	private boolean closed;
	private Instant createdAt;
	private Instant updatedAt;

	protected StoreHour() {
	}

	StoreHour(Store store, StoreModels.HourInput input, Instant now) {
		this.id = UUID.randomUUID();
		this.store = store;
		this.weekday = (short) input.weekday();
		this.opensAt = input.closed() ? null : input.opensAt();
		this.closesAt = input.closed() ? null : input.closesAt();
		this.closed = input.closed();
		this.createdAt = now;
		this.updatedAt = now;
	}

	StoreModels.HourView view() {
		return new StoreModels.HourView(weekday, opensAt, closesAt, closed);
	}
}
