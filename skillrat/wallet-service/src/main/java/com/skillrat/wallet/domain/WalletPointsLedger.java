package com.skillrat.wallet.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "wallet_points_ledger")
@Getter
@Setter
@NoArgsConstructor
public class WalletPointsLedger extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PointsCategory category;

    @Column(nullable = false)
    private int delta;

    @Column(length = 200)
    private String reason;

    @Column(name = "related_id")
    private UUID relatedId;
}
