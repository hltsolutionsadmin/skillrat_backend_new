package com.skillrat.wallet.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "wallet_points_category_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_wallet_user_category", columnNames = {"user_id", "category"}))
@Getter
@Setter
@NoArgsConstructor
public class WalletPointsCategoryProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PointsCategory category;

    @Column(nullable = false)
    private int earned;

    @Column(nullable = false)
    private int actions;
}
