package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;

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
@Table(name = "top_event_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Top Event", description = "Displays a single featured event")
public class TopEventComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Title", type = "string", required = true, placeholder = "e.g., Don't Miss This Event")
    private String title;

    @Column(name = "event_id")
    @CmsField(displayName = "Featured Event", type = "item:event", required = true, placeholder = "Select an event...")
    private String eventId;

    @Override
    public ComponentType getType() {
        return ComponentType.TOP_EVENT;
    }
}
