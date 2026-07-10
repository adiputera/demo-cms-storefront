package id.adiputera.demo.cms.storefront.service;

import id.adiputera.demo.cms.dto.ProductDTO;
import id.adiputera.demo.cms.entity.Product;
import id.adiputera.demo.cms.mapper.EntityMapper;
import id.adiputera.demo.cms.storefront.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.storefront.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Product Service class.
 *
 * @author Yusuf F. Adiputera
 */
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
        List<Product> products = productRepository.findAllOnline();
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

    /**
     * Fetches products by their IDs.
     *
     * @param ids The list of product IDs.
     * @return The list of product DTOs.
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        log.debug("Fetching products with ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findByIdIn(ids);
        return entityMapper.toProductDTOList(products);
    }
}
