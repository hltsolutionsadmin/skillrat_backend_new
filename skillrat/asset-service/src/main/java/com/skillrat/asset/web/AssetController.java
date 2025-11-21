package com.skillrat.asset.web;

import com.skillrat.asset.domain.Asset;
import com.skillrat.asset.domain.AssetCategory;
import com.skillrat.asset.repository.AssetCategoryRepository;
import com.skillrat.asset.repository.AssetRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetRepository repo;
    private final AssetCategoryRepository categoryRepository;

    public AssetController(AssetRepository repo, AssetCategoryRepository categoryRepository) {
        this.repo = repo;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Asset> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Asset> create(@Valid @RequestBody Asset in) {
        if (in.getCategory() != null && in.getCategory().getId() != null) {
            AssetCategory cat = categoryRepository.findById(in.getCategory().getId()).orElse(null);
            in.setCategory(cat);
        }
        Asset saved = repo.save(in);
        return ResponseEntity.created(URI.create("/api/assets/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> update(@PathVariable UUID id, @Valid @RequestBody Asset in) {
        return repo.findById(id).map(existing -> {
            if (in.getCategory() != null && in.getCategory().getId() != null) {
                AssetCategory cat = categoryRepository.findById(in.getCategory().getId()).orElse(null);
                existing.setCategory(cat);
            }
            existing.setName(in.getName());
            existing.setDescription(in.getDescription());
            existing.setStorageKey(in.getStorageKey());
            existing.setMimeType(in.getMimeType());
            existing.setSizeBytes(in.getSizeBytes());
            existing.setChecksum(in.getChecksum());
            existing.setVisibility(in.getVisibility());
            existing.setOwnerType(in.getOwnerType());
            existing.setOwnerId(in.getOwnerId());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
