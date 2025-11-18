package com.skillrat.project.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_group_member")
@Getter
@Setter
@NoArgsConstructor
public class UserGroupMember extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    @JsonBackReference
    private UserGroup group;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserGroupRole role = UserGroupRole.MEMBER;

    @Column(nullable = false)
    private boolean active = true;
}
