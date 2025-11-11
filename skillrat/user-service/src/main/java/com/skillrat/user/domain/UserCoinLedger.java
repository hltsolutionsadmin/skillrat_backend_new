package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_coin_ledger")
@Getter
@Setter
@NoArgsConstructor
public class UserCoinLedger extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CoinCategory category;

    @Column(nullable = false)
    private int delta; // positive for earn, negative for spend (future)

    @Column(length = 200)
    private String reason;

    @Column(name = "related_id")
    private UUID relatedId; // experience/skill/education/title id
}
