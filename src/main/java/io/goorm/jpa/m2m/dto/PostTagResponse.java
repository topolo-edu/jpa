package io.goorm.jpa.m2m.dto;

import io.goorm.jpa.m2m.entity.PostTag;

import java.time.LocalDateTime;

/**
 * PostTag 조회 응답 DTO
 *
 * 엔티티를 직접 반환하지 않고 필요한 데이터만 담아 반환
 */
public record PostTagResponse(
        Long id,
        String tagName,
        LocalDateTime taggedAt,
        Integer displayOrder,
        String taggedBy
) {
    /**
     * PostTag 엔티티를 DTO로 변환
     */
    public static PostTagResponse from(PostTag postTag) {
        return new PostTagResponse(
                postTag.getId(),
                postTag.getTag().getName(),  // Tag 엔티티에서 이름만 추출
                postTag.getTaggedAt(),
                postTag.getDisplayOrder(),
                postTag.getTaggedBy()
        );
    }
}
