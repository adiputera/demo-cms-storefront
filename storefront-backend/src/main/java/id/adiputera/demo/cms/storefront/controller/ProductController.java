package id.adiputera.demo.cms.storefront.controller;

import id.adiputera.demo.cms.dto.ProductDTO;
import id.adiputera.demo.cms.storefront.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Product Controller class.
 *
 * @author Yusuf F. Adiputera
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products");

        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ProductDTO> getProductByCode(@PathVariable("code") String code) {
        log.info("GET /api/products/{}", code);

        ProductDTO product = productService.getProductByCode(code);
        return ResponseEntity.ok(product);
    }

    /**
     * Gets online products by their codes.
     *
     * @param codes The list of product codes.
     * @return The list of product DTOs.
     */
    @GetMapping("/by-codes")
    public ResponseEntity<List<ProductDTO>> getProductsByCodes(@org.springframework.web.bind.annotation.RequestParam("codes") List<String> codes) {
        log.info("GET /api/products/by-codes?codes={}", codes);

        List<ProductDTO> products = productService.getProductsByCodes(codes);
        return ResponseEntity.ok(products);
    }
}
