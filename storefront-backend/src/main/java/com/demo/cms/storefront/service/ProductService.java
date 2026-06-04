package com.demo.cms.storefront.service;

import com.demo.cms.dto.ProductDTO;
import com.demo.cms.entity.Product;
import com.demo.cms.storefront.exception.ResourceNotFoundException;
import com.demo.cms.mapper.EntityMapper;
import com.demo.cms.storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Cacheable(value = "products", key = "'all'")
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");

        List<Product> products = productRepository.findAll();
        return entityMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "#code")
    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        log.debug("Fetching product with code: {}", code);

        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Product", code));

        return entityMapper.toProductDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCodes(List<String> codes) {
        log.debug("Fetching products with codes: {}", codes);

        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findByCodeIn(codes);
        return entityMapper.toProductDTOList(products);
    }
}
