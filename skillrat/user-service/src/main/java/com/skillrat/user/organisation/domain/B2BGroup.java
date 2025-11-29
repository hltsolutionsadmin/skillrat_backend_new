package com.skillrat.user.organisation.domain;

import com.skillrat.common.orm.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "b2b_group",
        indexes = {
                @Index(name = "idx_b2b_group_name_tenant", columnList = "name, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class B2BGroup extends BaseEntity {
	
	@Column(nullable = false, length = 40)
	private String code;
	
    @Column(nullable = false, length = 200)
    private String name;

    @OneToMany(mappedBy = "group")
    @JsonIgnore
    private List<B2BUnit> units = new ArrayList<>();
}
