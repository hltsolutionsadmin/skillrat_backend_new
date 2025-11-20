package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_coin_category_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_category", columnNames = {"user_id", "category"}))
public class UserCoinCategoryProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CoinCategory category;

    @Column(nullable = false)
    private int earned; // total coins earned in this category

    @Column(nullable = false)
    private int actions; // number of actions credited in this category
    
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
    
    public int getEarned() {
        return earned;
    }
    
    public void setEarned(int earned) {
        this.earned = earned;
    }
    
    public int getActions() {
        return actions;
    }
    
    public void setActions(int actions) {
        this.actions = actions;
    }
}
