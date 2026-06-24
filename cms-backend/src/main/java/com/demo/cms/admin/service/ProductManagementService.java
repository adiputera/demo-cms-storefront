package com.demo.cms.admin.service;

import com.demo.cms.dto.ProductDTO;
import com.demo.cms.entity.Product;
import com.demo.cms.entity.Catalog;
import com.demo.cms.entity.CatalogVersion;
import com.demo.cms.admin.repository.ProductRepository;
import com.demo.cms.admin.repository.CatalogRepository;
import com.demo.cms.admin.exception.ResourceNotFoundException;
import com.demo.cms.admin.exception.DuplicateResourceException;
import com.demo.cms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductManagementService {

    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;
    private final EntityMapper entityMapper;
    private final CatalogSyncService catalogSyncService;

    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("productCatalog", CatalogVersion.STAGED)
                .orElseGet(() -> catalogRepository.save(Catalog.builder()
                        .catalogId("productCatalog")
                        .version(CatalogVersion.STAGED)
                        .build()));
    }

    @Transactional
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all STAGED products");
        Catalog stagedCatalog = getStagedCatalog();
        List<Product> products = productRepository.findAllByCatalog(stagedCatalog, Pageable.unpaged()).getContent();
        
        Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(products, Product.class);
        
        return products.stream().map(product -> {
            ProductDTO dto = entityMapper.toProductDTO(product);
            dto.setSyncStatus(syncStatusMap.getOrDefault(product.getSyncKey(), "UNKNOWN"));
            dto.setSyncVersion(product.getSyncVersion());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return entityMapper.toProductDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with code: {}", productDTO.getCode());
        Catalog stagedCatalog = getStagedCatalog();
        
        if (productRepository.existsByCodeAndCatalog(productDTO.getCode(), stagedCatalog)) {
            throw new DuplicateResourceException("Product", "code", productDTO.getCode());
        }

        Product product = new Product();
        product.setCode(productDTO.getCode());
        product.setName(productDTO.getName());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setCatalog(stagedCatalog);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return entityMapper.toProductDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);
        Catalog stagedCatalog = getStagedCatalog();
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // Check if code is being changed and if new code already exists
        if (!product.getCode().equals(productDTO.getCode()) && 
            productRepository.existsByCodeAndCatalog(productDTO.getCode(), stagedCatalog)) {
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
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }
}
