package com.demo.cms.admin.controller;

import com.demo.cms.admin.dto.ApiResponse;
import com.demo.cms.admin.dto.CreateProductRequest;
import com.demo.cms.admin.service.ProductManagementService;
import com.demo.cms.dto.ProductDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cms/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductManagementController {

    private final ProductManagementService productManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        log.info("GET /api/cms/products");
        List<ProductDTO> products = productManagementService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable("id") Long id) {
        log.info("GET /api/cms/products/{}", id);
        ProductDTO product = productManagementService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/cms/products - Creating product with code: {}", request.getCode());
        
        ProductDTO productDTO = ProductDTO.builder()
                .code(request.getCode())
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();

        ProductDTO createdProduct = productManagementService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", createdProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateProductRequest request) {
        log.info("PUT /api/cms/products/{}", id);
        
        ProductDTO productDTO = ProductDTO.builder()
                .code(request.getCode())
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();

        ProductDTO updatedProduct = productManagementService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") Long id) {
        log.info("DELETE /api/cms/products/{}", id);
        productManagementService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
