package io.goorm.jpa.service;

import io.goorm.jpa.dto.ProductCreateRequest;
import io.goorm.jpa.dto.ProductResponse;
import io.goorm.jpa.dto.ProductUpdateRequest;
import io.goorm.jpa.entity.Product;
import io.goorm.jpa.exception.ProductNotFoundException;
import io.goorm.jpa.mapper.ProductMapper;
import io.goorm.jpa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * 상품 생성 (Create)
     */
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = Product.of(
                request.productName(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    /**
     * 상품 수정 (Update)
     */
    @Transactional
    public ProductResponse update(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 변경 감지(Dirty Checking)로 자동 UPDATE
        product.update(
                request.productName(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        return productMapper.toResponse(product);
    }

    /**
     * 상품 삭제 (Delete)
     */
    @Transactional
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        productRepository.delete(product);
    }
}
