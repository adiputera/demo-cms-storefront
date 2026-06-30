package id.adiputera.demo.cms.dto;

import java.util.List;
import id.adiputera.demo.cms.entity.ComponentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class LatestEventComponentDTO extends ComponentDTO {
    private String title;
    private List<String> eventIds;

    public LatestEventComponentDTO() {
        super();
        this.setType(ComponentType.LATEST_EVENT.name());
    }
}
