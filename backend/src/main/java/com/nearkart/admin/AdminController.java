package com.nearkart.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nearkart.common.ApiResponse;
import com.nearkart.common.PageView;
import com.nearkart.user.UserStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController {
	private final AdminService admin;
	AdminController(AdminService admin) { this.admin = admin; }

	@GetMapping("/dashboard")
	ApiResponse<AdminService.DashboardView> dashboard() { return ApiResponse.success(admin.dashboard()); }
	@GetMapping("/users")
	ApiResponse<PageView<AdminService.UserView>> users(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
		return ApiResponse.success(admin.users(page, size));
	}
	@PatchMapping("/users/{id}")
	ApiResponse<Void> user(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody UserRequest request) {
		admin.updateUser(subject(jwt), id, request.status()); return ApiResponse.success(null);
	}
	@GetMapping("/reviews")
	ApiResponse<PageView<AdminService.ReviewView>> reviews(@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
		return ApiResponse.success(admin.reviews(status, page, size));
	}
	@PatchMapping("/reviews/{id}")
	ApiResponse<Void> review(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody ReviewRequest request) {
		admin.moderateReview(subject(jwt), id, request.status()); return ApiResponse.success(null);
	}
	@GetMapping("/reports")
	ApiResponse<PageView<AdminService.ReportView>> reports(@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
		return ApiResponse.success(admin.reports(status, page, size));
	}
	@PatchMapping("/reports/{id}")
	ApiResponse<Void> report(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody ReportRequest request) {
		admin.resolveReport(subject(jwt), id, request.status()); return ApiResponse.success(null);
	}
	@GetMapping("/audit-logs")
	ApiResponse<PageView<AdminService.AuditView>> audits(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
		return ApiResponse.success(admin.audits(page, size));
	}
	@GetMapping("/catalog/categories")
	ApiResponse<List<AdminService.CategoryView>> categories() { return ApiResponse.success(admin.categories()); }
	@GetMapping("/catalog/products")
	ApiResponse<PageView<AdminService.ProductView>> products(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
		return ApiResponse.success(admin.products(page, size));
	}
	@PatchMapping("/catalog/products/{id}")
	ApiResponse<Void> product(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody ProductRequest request) {
		admin.updateProduct(subject(jwt), id, request.status()); return ApiResponse.success(null);
	}
	private static UUID subject(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

	record UserRequest(@NotNull UserStatus status) {}
	record ReviewRequest(@NotNull AdminService.ReviewStatus status) {}
	record ReportRequest(@NotNull AdminService.ReportStatus status) {}
	record ProductRequest(@NotNull AdminService.ProductStatus status) {}
}
