package com.skillrat.asset.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset")
public class Asset extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @Column(name = "business_id", nullable = false, length = 64, unique = true)
    private String businessId;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 16)
    private Visibility visibility = Visibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 32)
    private OwnerType ownerType;

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    public enum Visibility { PUBLIC, PRIVATE }
    public enum OwnerType { ORG, PROJECT, USER }
}
