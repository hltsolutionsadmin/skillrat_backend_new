package com.skillrat.asset.web;

import com.skillrat.asset.domain.AssetCategory;
import com.skillrat.asset.repository.AssetCategoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asset-categories")
public class AssetCategoryController {
    private final AssetCategoryRepository repo;

    public AssetCategoryController(AssetCategoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<AssetCategory> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<AssetCategory> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AssetCategory> create(@Valid @RequestBody AssetCategory in) {
        AssetCategory saved = repo.save(in);
        return ResponseEntity.created(URI.create("/api/asset-categories/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetCategory> update(@PathVariable UUID id, @Valid @RequestBody AssetCategory in) {
        return repo.findById(id).map(existing -> {
            existing.setCode(in.getCode());
            existing.setName(in.getName());
            existing.setDescription(in.getDescription());
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
