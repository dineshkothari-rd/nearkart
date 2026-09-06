package com.nearkart.store;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.text.Normalizer;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nearkart.audit.AuditLog;
import com.nearkart.audit.AuditLogRepository;
import com.nearkart.common.ApiException;
import com.nearkart.store.StoreModels.ApprovalRequest;
import com.nearkart.store.StoreModels.CreateStoreRequest;
import com.nearkart.store.StoreModels.HoursRequest;
import com.nearkart.store.StoreModels.StoreView;
import com.nearkart.store.StoreModels.UpdateStoreRequest;
import com.nearkart.user.User;
import com.nearkart.user.UserRepository;
import com.nearkart.user.UserStatus;

@Service
class StoreService {

	private final StoreRepository stores;
	private final UserRepository users;
	private final AuditLogRepository auditLogs;
	private final Clock clock;

	StoreService(StoreRepository stores, UserRepository users, AuditLogRepository auditLogs, Clock clock) {
		this.stores = stores;
		this.users = users;
		this.auditLogs = auditLogs;
		this.clock = clock;
	}

	@Transactional
	StoreView create(UUID ownerId, CreateStoreRequest request) {
		User owner = activeUser(ownerId);
		validateTimezone(request.timezone());
		Instant now = clock.instant();
		String slug = slug(request.name()) + "-" + UUID.randomUUID().toString().substring(0, 8);
		Store store = stores.save(new Store(owner, request, slug, now));
		auditLogs.save(new AuditLog(owner, "STORE_SUBMITTED", "STORE", store.id(), Map.of(), now));
		return view(store);
	}

	@Transactional(readOnly = true)
	Page<StoreView> owned(UUID ownerId, Pageable pageable) {
		return stores.findAllByOwnerId(ownerId, pageable).map(this::view);
	}

	@Transactional(readOnly = true)
	StoreView owned(UUID ownerId, UUID storeId) {
		return view(ownedStore(ownerId, storeId));
	}

	@Transactional
	StoreView update(UUID ownerId, UUID storeId, UpdateStoreRequest request) {
		Store store = ownedStore(ownerId, storeId);
		validateOptionalText(request.name(), "Store name");
		validateOptionalText(request.timezone(), "Timezone");
		if (request.timezone() != null) validateTimezone(request.timezone());
		Instant now = clock.instant();
		store.update(request.name(), request.description(), request.phone(), request.timezone(), request.location(), now);
		auditLogs.save(new AuditLog(activeUser(ownerId), "STORE_UPDATED", "STORE", store.id(), Map.of(), now));
		return view(store);
	}

	@Transactional
	StoreView replaceHours(UUID ownerId, UUID storeId, HoursRequest request) {
		validateHours(request);
		Store store = ownedStore(ownerId, storeId);
		Instant now = clock.instant();
		store.replaceHours(request.hours(), now);
		auditLogs.save(new AuditLog(activeUser(ownerId), "STORE_HOURS_UPDATED", "STORE", store.id(), Map.of(), now));
		return view(store);
	}

	@Transactional(readOnly = true)
	StoreView publicStore(UUID storeId) {
		return stores.findByIdAndStatus(storeId, StoreStatus.APPROVED)
			.map(this::view)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "Store not found"));
	}

	@Transactional(readOnly = true)
	Page<StoreView> adminStores(StoreStatus status, Pageable pageable) {
		return (status == null ? stores.findAll(pageable) : stores.findAllByStatus(status, pageable)).map(this::view);
	}

	@Transactional
	StoreView decide(UUID adminId, UUID storeId, ApprovalRequest request) {
		if (request.decision() == StoreModels.ApprovalDecision.REJECT
				&& (request.reason() == null || request.reason().isBlank())) {
			throw new ApiException(BAD_REQUEST, "A rejection reason is required");
		}
		User admin = activeUser(adminId);
		Store store = stores.findById(storeId)
			.filter(candidate -> candidate.status() == StoreStatus.PENDING)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "Pending store not found"));
		Instant now = clock.instant();
		store.decide(admin, request.decision(), request.reason(), now);
		auditLogs.save(new AuditLog(admin,
			request.decision() == StoreModels.ApprovalDecision.APPROVE ? "STORE_APPROVED" : "STORE_REJECTED",
			"STORE", store.id(), Map.of("reason", request.reason() == null ? "" : request.reason().trim()), now));
		return view(store);
	}

	private Store ownedStore(UUID ownerId, UUID storeId) {
		return stores.findByIdAndOwnerId(storeId, ownerId)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "Store not found"));
	}

	private User activeUser(UUID id) {
		return users.findById(id).filter(user -> user.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(() -> new ApiException(NOT_FOUND, "User not found"));
	}

	private StoreView view(Store store) {
		return new StoreView(store.id(), store.ownerId(), store.name(), store.slug(), store.description(),
			store.phone(), store.timezone(), store.status(), store.approvalReason(), store.version(),
			store.location().view(), store.hours().stream().map(StoreHour::view)
				.sorted(java.util.Comparator.comparingInt(StoreModels.HourView::weekday)).toList(),
			store.createdAt(), store.updatedAt());
	}

	private static String slug(String name) {
		String value = Normalizer.normalize(name, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.toLowerCase(java.util.Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("(^-|-$)", "");
		return value.isBlank() ? "store" : value;
	}

	private static void validateTimezone(String timezone) {
		try {
			ZoneId.of(timezone.trim());
		} catch (DateTimeException exception) {
			throw new ApiException(BAD_REQUEST, "Invalid timezone");
		}
	}

	private static void validateOptionalText(String value, String label) {
		if (value != null && value.isBlank()) throw new ApiException(BAD_REQUEST, label + " cannot be blank");
	}

	private static void validateHours(HoursRequest request) {
		var weekdays = new HashSet<Integer>();
		request.hours().forEach(hour -> {
			if (!weekdays.add(hour.weekday())) throw new ApiException(BAD_REQUEST, "Duplicate weekday");
			if (hour.closed() && (hour.opensAt() != null || hour.closesAt() != null))
				throw new ApiException(BAD_REQUEST, "Closed days cannot have opening times");
			if (!hour.closed() && (hour.opensAt() == null || hour.closesAt() == null
					|| hour.opensAt().equals(hour.closesAt())))
				throw new ApiException(BAD_REQUEST, "Open days require different opening and closing times");
		});
	}
}
