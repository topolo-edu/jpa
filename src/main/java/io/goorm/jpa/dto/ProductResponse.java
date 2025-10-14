package io.goorm.jpa.dto;

import io.goorm.jpa.enums.ProductStatus;

import java.time.LocalDateTime;

/**
 * 상품 응답 DTO
 * MapStruct를 통해 Product 엔티티로부터 자동 매핑됨
 */
public record ProductResponse(
        Long productId,
        String productName,
        String description,
        Integer price,
        Integer stockQuantity,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
