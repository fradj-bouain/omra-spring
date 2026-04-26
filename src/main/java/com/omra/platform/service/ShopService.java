package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.Agency;
import com.omra.platform.entity.Marketplace;
import com.omra.platform.entity.MarketplaceOrder;
import com.omra.platform.entity.MarketplaceProduct;
import com.omra.platform.entity.enums.AgencyKind;
import com.omra.platform.entity.enums.MarketplaceCatalogType;
import com.omra.platform.entity.enums.MarketplaceStatus;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.MarketplaceOrderRepository;
import com.omra.platform.repository.MarketplaceProductRepository;
import com.omra.platform.repository.MarketplaceRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final AgencyRepository agencyRepository;
    private final MarketplaceRepository marketplaceRepository;
    private final MarketplaceProductRepository productRepository;
    private final MarketplaceOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public MarketplaceDto getOrCreateMarketplace() {
        Long agencyId = requireMarketplaceAgencyId();
        List<Marketplace> list = marketplaceRepository.findByAgencyIdOrderByNameAsc(agencyId);
        if (list.isEmpty()) {
            return toMarketplaceDto(createDefaultMarketplace(agencyId));
        }
        return toMarketplaceDto(list.get(0));
    }

    @Transactional
    public MarketplaceDto updateMarketplace(MarketplaceWriteDto dto) {
        Long agencyId = requireMarketplaceAgencyId();
        Marketplace m = firstOrCreate(agencyId);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            m.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            m.setDescription(dto.getDescription().isBlank() ? null : dto.getDescription());
        }
        if (dto.getStatus() != null) {
            m.setStatus(dto.getStatus());
        }
        if (dto.getCatalogType() != null) {
            m.setCatalogType(dto.getCatalogType());
        }
        if (m.getCatalogType() == MarketplaceCatalogType.MANUAL) {
            m.setApiBaseUrl(null);
            m.setApiAuthHeader(null);
            m.setApiAuthValue(null);
        } else {
            if (dto.getApiBaseUrl() != null) {
                m.setApiBaseUrl(dto.getApiBaseUrl().isBlank() ? null : dto.getApiBaseUrl().trim());
            }
            if (dto.getApiAuthHeader() != null) {
                m.setApiAuthHeader(dto.getApiAuthHeader().isBlank() ? null : dto.getApiAuthHeader().trim());
            }
            if (dto.getApiAuthValue() != null) {
                m.setApiAuthValue(dto.getApiAuthValue().isBlank() ? null : dto.getApiAuthValue());
            }
        }
        if (m.getCatalogType() == MarketplaceCatalogType.EXTERNAL_API
                && (m.getApiBaseUrl() == null || m.getApiBaseUrl().isBlank())) {
            throw new BadRequestException("apiBaseUrl est requis pour une API externe.");
        }
        m = marketplaceRepository.save(m);
        return toMarketplaceDto(m);
    }

    @Transactional(readOnly = true)
    public List<MarketplaceProductDto> listProducts() {
        Marketplace m = loadShopMarketplace();
        return productRepository.findByAgencyIdAndMarketplaceIdOrderByTitleAsc(m.getAgencyId(), m.getId()).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MarketplaceProductDto createProduct(ShopProductWriteDto dto) {
        Marketplace m = loadShopMarketplace();
        validateProductWrite(dto);
        Agency agency = agencyRepository.findById(m.getAgencyId()).orElseThrow();
        String currency = dto.getCurrency() != null && !dto.getCurrency().isBlank()
                ? dto.getCurrency().trim()
                : defaultCurrency(agency);
        MarketplaceProduct p = MarketplaceProduct.builder()
                .agencyId(m.getAgencyId())
                .marketplaceId(m.getId())
                .title(dto.getTitle().trim())
                .description(dto.getDescription() != null && !dto.getDescription().isBlank() ? dto.getDescription() : null)
                .imageUrl(dto.getImageUrl() != null && !dto.getImageUrl().isBlank() ? dto.getImageUrl().trim() : null)
                .price(dto.getPrice())
                .currency(currency)
                .inStock(dto.getInStock() == null || dto.getInStock())
                .stockQuantity(dto.getStockQuantity())
                .build();
        return toProductDto(productRepository.save(p));
    }

    @Transactional
    public MarketplaceProductDto updateProduct(Long productId, ShopProductWriteDto dto) {
        Marketplace m = loadShopMarketplace();
        MarketplaceProduct p = productRepository.findByIdAndAgencyId(productId, m.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!p.getMarketplaceId().equals(m.getId())) {
            throw new ForbiddenException("Produit hors boutique.");
        }
        validateProductWrite(dto);
        p.setTitle(dto.getTitle().trim());
        p.setDescription(dto.getDescription() != null && !dto.getDescription().isBlank() ? dto.getDescription() : null);
        p.setImageUrl(dto.getImageUrl() != null && !dto.getImageUrl().isBlank() ? dto.getImageUrl().trim() : null);
        p.setPrice(dto.getPrice());
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            p.setCurrency(dto.getCurrency().trim());
        }
        if (dto.getInStock() != null) {
            p.setInStock(dto.getInStock());
        }
        if (dto.getStockQuantity() != null) {
            p.setStockQuantity(dto.getStockQuantity());
        }
        return toProductDto(productRepository.save(p));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Marketplace m = loadShopMarketplace();
        MarketplaceProduct p = productRepository.findByIdAndAgencyId(productId, m.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!p.getMarketplaceId().equals(m.getId())) {
            throw new ForbiddenException("Produit hors boutique.");
        }
        productRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<ShopOrderSummaryDto> listOrders(int page, int size) {
        Long agencyId = requireMarketplaceAgencyId();
        Page<MarketplaceOrder> pg = orderRepository.findByAgencyIdOrderByCreatedAtDesc(agencyId, PageRequest.of(page, size));
        List<ShopOrderSummaryDto> content = pg.getContent().stream()
                .map(o -> ShopOrderSummaryDto.builder()
                        .id(o.getId())
                        .pilgrimId(o.getPilgrimId())
                        .status(o.getStatus())
                        .total(o.getTotal())
                        .currency(o.getCurrency())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return PageResponse.<ShopOrderSummaryDto>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .first(pg.isFirst())
                .last(pg.isLast())
                .build();
    }

    private Long requireMarketplaceAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Agence requise.");
        }
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
        if (agency.getAgencyKind() != AgencyKind.MARKETPLACE) {
            throw new ForbiddenException("Réservé aux agences de type marketplace.");
        }
        return agencyId;
    }

    private Marketplace loadShopMarketplace() {
        Long agencyId = requireMarketplaceAgencyId();
        return firstOrCreate(agencyId);
    }

    private Marketplace firstOrCreate(Long agencyId) {
        List<Marketplace> list = marketplaceRepository.findByAgencyIdOrderByNameAsc(agencyId);
        if (list.isEmpty()) {
            return createDefaultMarketplace(agencyId);
        }
        return list.get(0);
    }

    private Marketplace createDefaultMarketplace(Long agencyId) {
        Marketplace m = Marketplace.builder()
                .agencyId(agencyId)
                .name("Boutique")
                .description(null)
                .status(MarketplaceStatus.ACTIVE)
                .catalogType(MarketplaceCatalogType.MANUAL)
                .build();
        return marketplaceRepository.save(m);
    }

    private void validateProductWrite(ShopProductWriteDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("title est requis.");
        }
        if (dto.getPrice() == null) {
            throw new BadRequestException("price est requis.");
        }
    }

    private String defaultCurrency(Agency agency) {
        String c = agency.getCurrency();
        return c != null && !c.isBlank() ? c.trim() : "MAD";
    }

    private MarketplaceDto toMarketplaceDto(Marketplace m) {
        return MarketplaceDto.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .status(m.getStatus())
                .catalogType(m.getCatalogType())
                .apiBaseUrl(m.getApiBaseUrl())
                .apiAuthHeader(m.getApiAuthHeader())
                .apiAuthValue(m.getApiAuthValue())
                .build();
    }

    private MarketplaceProductDto toProductDto(MarketplaceProduct p) {
        return MarketplaceProductDto.builder()
                .id(p.getId())
                .marketplaceId(p.getMarketplaceId())
                .title(p.getTitle())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .price(p.getPrice())
                .currency(p.getCurrency())
                .inStock(p.isInStock())
                .stockQuantity(p.getStockQuantity())
                .build();
    }
}
