package com.example.inventoryservice.repository;

import java.util.Optional;

import com.example.inventoryservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);
}
