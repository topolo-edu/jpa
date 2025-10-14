package io.goorm.jpa.mapper;

import io.goorm.jpa.dto.ProductResponse;
import io.goorm.jpa.entity.Product;
import org.mapstruct.Mapper;

/**
 * Product 엔티티 <-> DTO 매핑을 위한 MapStruct Mapper
 *
 * componentModel = "spring": Spring Bean으로 등록하여 의존성 주입 가능
 * MapStruct가 컴파일 타임에 ProductMapperImpl 구현체를 자동 생성
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Product 엔티티를 ProductResponse DTO로 변환
     *
     * @param product 변환할 Product 엔티티
     * @return ProductResponse DTO
     */
    ProductResponse toResponse(Product product);
}
