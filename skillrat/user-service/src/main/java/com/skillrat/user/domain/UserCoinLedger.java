package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_coin_ledger")
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
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public CoinCategory getCategory() {
        return category;
    }
    
    public void setCategory(CoinCategory category) {
        this.category = category;
    }
    
    public int getDelta() {
        return delta;
    }
    
    public void setDelta(int delta) {
        this.delta = delta;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public UUID getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(UUID relatedId) {
        this.relatedId = relatedId;
    }
}
