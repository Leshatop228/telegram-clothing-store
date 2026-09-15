package com.labubu.telegramclothingstore.repository;

import com.labubu.telegramclothingstore.catalog.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
}