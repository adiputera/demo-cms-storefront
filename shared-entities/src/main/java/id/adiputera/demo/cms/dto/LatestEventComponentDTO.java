package id.adiputera.demo.cms.dto;

import id.adiputera.demo.cms.entity.ComponentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Latest Event Component D T O class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@Setter
@SuperBuilder
public class LatestEventComponentDTO extends ComponentDTO {
    private String title;
    private List<String> eventSlugs;

    public LatestEventComponentDTO() {
        super();
        this.setType(ComponentType.LATEST_EVENT.name());
    }
}
