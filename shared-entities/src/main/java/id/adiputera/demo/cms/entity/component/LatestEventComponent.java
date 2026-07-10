package id.adiputera.demo.cms.entity.component;

import id.adiputera.demo.cms.annotation.CmsComponent;
import id.adiputera.demo.cms.annotation.CmsField;
import id.adiputera.demo.cms.annotation.CmsFieldType;
import id.adiputera.demo.cms.entity.Component;
import id.adiputera.demo.cms.entity.ComponentType;
import id.adiputera.demo.cms.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Latest Event Component class.
 *
 * @author Yusuf F. Adiputera
 */
@Entity
@Table(name = "latest_event_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CmsComponent(displayName = "Latest Events", description = "Displays a list of the latest events")
public class LatestEventComponent extends Component {

    @Size(max = 255)
    @Column(name = "title")
    @CmsField(displayName = "Title", type = CmsFieldType.STRING, required = true, placeholder = "e.g., Upcoming Events")
    private String title;

    @Column(name = "event_slugs", columnDefinition = "TEXT")
    @CmsField(displayName = "Events", type = CmsFieldType.REFERENCE, targetEntity = Event.class, cardinality = id.adiputera.demo.cms.annotation.ReferenceCardinality.MULTIPLE, required = true, placeholder = "Select events...")
    private String eventSlugs;

    @Override
    public ComponentType getType() {
        return ComponentType.LATEST_EVENT;
    }
}
