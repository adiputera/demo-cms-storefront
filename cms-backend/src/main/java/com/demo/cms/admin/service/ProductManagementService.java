package com.demo.cms.admin.service;

import com.demo.cms.dto.ProductDTO;
import com.demo.cms.entity.Product;
import com.demo.cms.admin.repository.ProductRepository;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.exception.DuplicateResourceException;
import com.demo.cms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");
        List<Product> products = productRepository.findAll();
        return entityMapper.toProductDTOList(products);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return entityMapper.toProductDTO(product);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "#result.code")
    })
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with code: {}", productDTO.getCode());
        
        if (productRepository.existsByCode(productDTO.getCode())) {
            throw new DuplicateResourceException("Product", "code", productDTO.getCode());
        }

        Product product = Product.builder()
                .code(productDTO.getCode())
                .name(productDTO.getName())
                .imageUrl(productDTO.getImageUrl())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return entityMapper.toProductDTO(savedProduct);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "#productDTO.code")
    })
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // Check if code is being changed and if new code already exists
        if (!product.getCode().equals(productDTO.getCode()) && 
            productRepository.existsByCode(productDTO.getCode())) {
            throw new DuplicateResourceException("Product", "code", productDTO.getCode());
        }

        product.setCode(productDTO.getCode());
        product.setName(productDTO.getName());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return entityMapper.toProductDTO(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }
}
