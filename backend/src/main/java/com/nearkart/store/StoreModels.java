package com.nearkart.store;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class StoreModels {

	private StoreModels() {
	}

	public record LocationInput(
		@NotBlank @Size(max = 250) String addressLine,
		@NotBlank @Size(max = 100) String locality,
		@NotBlank @Size(max = 100) String city,
		@NotBlank @Size(max = 100) String state,
		@NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$") String postalCode,
		@NotNull @Digits(integer = 2, fraction = 6) @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
		@NotNull @Digits(integer = 3, fraction = 6) @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude) {
	}

	public record CreateStoreRequest(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 1000) String description,
		@NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
		@NotBlank @Size(max = 64) String timezone,
		@NotNull @Valid LocationInput location) {
	}

	public record UpdateStoreRequest(
		@Size(min = 1, max = 150) String name,
		@Size(max = 1000) String description,
		@Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
		@Size(min = 1, max = 64) String timezone,
		@Valid LocationInput location) {
	}

	public record HourInput(
		@Min(1) @Max(7) int weekday,
		LocalTime opensAt,
		LocalTime closesAt,
		boolean closed) {
	}

	public record HoursRequest(@NotNull @Size(max = 7) List<@Valid HourInput> hours) {
	}

	public enum ApprovalDecision { APPROVE, REJECT }

	public record ApprovalRequest(
		@NotNull ApprovalDecision decision,
		@Size(max = 500) String reason) {
	}

	public record LocationView(String addressLine, String locality, String city, String state,
		String postalCode, BigDecimal latitude, BigDecimal longitude) {
	}

	public record HourView(int weekday, LocalTime opensAt, LocalTime closesAt, boolean closed) {
	}

	public record StoreView(UUID id, UUID ownerId, String name, String slug, String description,
		String phone, String timezone, StoreStatus status, String approvalReason, int version,
		LocationView location, List<HourView> hours, Instant createdAt, Instant updatedAt) {
	}
}
