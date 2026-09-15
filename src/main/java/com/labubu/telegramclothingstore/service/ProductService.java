package com.labubu.telegramclothingstore.service;

import com.labubu.telegramclothingstore.catalog.CategoryEntity;
import com.labubu.telegramclothingstore.repository.CategoryRepository;
import com.labubu.telegramclothingstore.catalog.ProductEntity;
import com.labubu.telegramclothingstore.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductEntity> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductEntity createProduct(Long categoryId, String title, String description,
                                       String size, String material, BigDecimal price,
                                       String avitoUrl, String photoUrl) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория с id " + categoryId + " не найдена"));

        ProductEntity product = new ProductEntity();
        product.setCategory(category);
        product.setTitle(title);
        product.setDescription(description);
        product.setSize(size);
        product.setMaterial(material);
        product.setPrice(price);
        product.setAvitoUrl(avitoUrl);
        product.setPhotoUrl(photoUrl);
        product.setCreatedAt(OffsetDateTime.now());

        return productRepository.save(product);
    }
}
