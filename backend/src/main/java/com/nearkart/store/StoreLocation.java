package com.nearkart.store;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_locations")
class StoreLocation {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_id")
	private Store store;

	private String addressLine;
	private String locality;
	private String city;
	private String state;
	private String postalCode;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private Instant createdAt;
	private Instant updatedAt;

	protected StoreLocation() {
	}

	StoreLocation(Store store, StoreModels.LocationInput input, Instant now) {
		this.id = UUID.randomUUID();
		this.store = store;
		update(input, now);
		this.createdAt = now;
	}

	void update(StoreModels.LocationInput input, Instant now) {
		this.addressLine = input.addressLine().trim();
		this.locality = input.locality().trim();
		this.city = input.city().trim();
		this.state = input.state().trim();
		this.postalCode = input.postalCode().trim();
		this.latitude = input.latitude();
		this.longitude = input.longitude();
		this.updatedAt = now;
	}

	StoreModels.LocationView view() {
		return new StoreModels.LocationView(addressLine, locality, city, state, postalCode, latitude, longitude);
	}
}
