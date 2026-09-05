package com.nearkart.store;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	Optional<Store> findByIdAndOwnerId(UUID id, UUID ownerId);

	Optional<Store> findByIdAndStatus(UUID id, StoreStatus status);

	Page<Store> findAllByOwnerId(UUID ownerId, Pageable pageable);

	Page<Store> findAllByStatus(StoreStatus status, Pageable pageable);
}
