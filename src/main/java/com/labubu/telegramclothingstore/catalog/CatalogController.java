package com.labubu.telegramclothingstore.catalog;

import com.labubu.telegramclothingstore.catalog.dto.CreateCategoryRequest;
import com.labubu.telegramclothingstore.catalog.dto.CreateProductRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
public class CatalogController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public CatalogController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    public List<CategoryEntity> getCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public CategoryEntity createCategory(@RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request.getName());
    }

    @GetMapping("/{categoryId}/products")
    public List<ProductEntity> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @PostMapping("/products")
    public ProductEntity createProduct(@RequestBody CreateProductRequest request) {
        return productService.createProduct(
                request.getCategoryId(),
                request.getTitle(),
                request.getDescription(),
                request.getSize(),
                request.getMaterial(),
                request.getAvitoUrl(),
                request.getPhotoUrl()
        );
    }
}