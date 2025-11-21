package com.skillrat.asset.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "asset_inventory")
public class AssetInventory extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(name = "location", length = 128)
    private String location;

    @Column(name = "quantity_total", nullable = false)
    private Integer quantityTotal = 0;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;
}
