package com.skillrat.project.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_group")
@Getter
@Setter
@NoArgsConstructor
public class UserGroup extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private UUID b2bUnitId;

    @Column
    private UUID projectId; // nullable: org-level group if null

    @Column
    private UUID leadId; // optional group lead

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UserGroupMember> members = new ArrayList<>();
}
