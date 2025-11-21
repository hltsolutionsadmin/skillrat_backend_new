package com.skillrat.asset.web;

import com.skillrat.asset.domain.Asset;
import com.skillrat.asset.domain.AssetRequest;
import com.skillrat.asset.repository.AssetRepository;
import com.skillrat.asset.repository.AssetRequestRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asset-requests")
public class AssetRequestController {
    private final AssetRequestRepository repo;
    private final AssetRepository assetRepository;

    public AssetRequestController(AssetRequestRepository repo, AssetRepository assetRepository) {
        this.repo = repo;
        this.assetRepository = assetRepository;
    }

    @GetMapping
    public List<AssetRequest> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<AssetRequest> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetRequest> create(@Valid @RequestBody AssetRequest in) {
        if (in.getAsset() != null && in.getAsset().getId() != null) {
            Asset a = assetRepository.findById(in.getAsset().getId()).orElse(null);
            in.setAsset(a);
        }
        AssetRequest saved = repo.save(in);
        return ResponseEntity.created(URI.create("/api/asset-requests/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetRequest> update(@PathVariable UUID id, @Valid @RequestBody AssetRequest in) {
        return repo.findById(id).map(existing -> {
            if (in.getAsset() != null && in.getAsset().getId() != null) {
                Asset a = assetRepository.findById(in.getAsset().getId()).orElse(null);
                existing.setAsset(a);
            }
            existing.setRequestedBy(in.getRequestedBy());
            existing.setQuantity(in.getQuantity());
            existing.setStatus(in.getStatus());
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
