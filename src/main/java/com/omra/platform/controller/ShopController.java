package com.omra.platform.controller;

import com.omra.platform.dto.*;
import com.omra.platform.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Single storefront for MARKETPLACE agencies")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/marketplace")
    @Operation(summary = "Get or bootstrap the agency shop settings")
    public ResponseEntity<MarketplaceDto> getMarketplace() {
        return ResponseEntity.ok(shopService.getOrCreateMarketplace());
    }

    @PutMapping("/marketplace")
    @Operation(summary = "Update shop settings")
    public ResponseEntity<MarketplaceDto> putMarketplace(@RequestBody MarketplaceWriteDto body) {
        return ResponseEntity.ok(shopService.updateMarketplace(body));
    }

    @GetMapping("/products")
    @Operation(summary = "List articles in the agency shop")
    public ResponseEntity<java.util.List<MarketplaceProductDto>> listProducts() {
        return ResponseEntity.ok(shopService.listProducts());
    }

    @PostMapping("/products")
    @Operation(summary = "Create article")
    public ResponseEntity<MarketplaceProductDto> createProduct(@RequestBody ShopProductWriteDto body) {
        return ResponseEntity.ok(shopService.createProduct(body));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update article")
    public ResponseEntity<MarketplaceProductDto> updateProduct(
            @PathVariable Long id, @RequestBody ShopProductWriteDto body) {
        return ResponseEntity.ok(shopService.updateProduct(id, body));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete article")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        shopService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    @Operation(summary = "List shop orders (paginated)")
    public ResponseEntity<PageResponse<ShopOrderSummaryDto>> listOrders(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(shopService.listOrders(page, size));
    }
}
