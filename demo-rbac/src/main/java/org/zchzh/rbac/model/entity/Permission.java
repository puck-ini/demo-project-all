package org.zchzh.rbac.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.http.HttpMethod;
import org.zchzh.rbac.type.PermissionType;
import org.zchzh.rbac.type.Url;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zengchzh
 * @date 2021/7/30
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity{


    private static final long serialVersionUID = 5098781407648716098L;
    /**
     * 权限名
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 权限类型
     */
    @Enumerated(EnumType.ORDINAL)
    private PermissionType type;
//    /**
//     * 权限类型为前端页面时表示前端url，权限类型为后端接口时表示后端接口url
//     */
//    private String url;
//    /**
//     * 请求方式
//     */
//    @Enumerated(EnumType.ORDINAL)
//    private HttpMethod method;

    private Url url;
//    /**
//     * 父级权限id
//     */
//    private Long parentId;

    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @ManyToMany(cascade = CascadeType.ALL, targetEntity = Role.class)
    @JoinTable(name = "role_permission",
            joinColumns = {@JoinColumn(name = "permission_id", referencedColumnName = "id")},
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private List<Role> roles = new ArrayList<>();
}
