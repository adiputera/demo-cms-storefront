package com.demo.cms.entity.component;

import com.demo.cms.entity.Component;
import com.demo.cms.entity.ComponentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_detail_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Column(name = "show_price")
    private Boolean showPrice;

    @Column(name = "show_description")
    private Boolean showDescription;

    @Override
    public ComponentType getType() {
        return ComponentType.PRODUCT_DETAIL;
    }
}
