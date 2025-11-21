package com.skillrat.asset.web;

import com.skillrat.asset.domain.Asset;
import com.skillrat.asset.domain.AssetInventory;
import com.skillrat.asset.repository.AssetInventoryRepository;
import com.skillrat.asset.repository.AssetRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asset-inventories")
public class AssetInventoryController {
    private final AssetInventoryRepository repo;
    private final AssetRepository assetRepository;

    public AssetInventoryController(AssetInventoryRepository repo, AssetRepository assetRepository) {
        this.repo = repo;
        this.assetRepository = assetRepository;
    }

    @GetMapping
    public List<AssetInventory> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<AssetInventory> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetInventory> create(@Valid @RequestBody AssetInventory in) {
        if (in.getAsset() != null && in.getAsset().getId() != null) {
            Asset a = assetRepository.findById(in.getAsset().getId()).orElse(null);
            in.setAsset(a);
        }
        AssetInventory saved = repo.save(in);
        return ResponseEntity.created(URI.create("/api/asset-inventories/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetInventory> update(@PathVariable UUID id, @Valid @RequestBody AssetInventory in) {
        return repo.findById(id).map(existing -> {
            if (in.getAsset() != null && in.getAsset().getId() != null) {
                Asset a = assetRepository.findById(in.getAsset().getId()).orElse(null);
                existing.setAsset(a);
            }
            existing.setLocation(in.getLocation());
            existing.setQuantityTotal(in.getQuantityTotal());
            existing.setQuantityAvailable(in.getQuantityAvailable());
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
