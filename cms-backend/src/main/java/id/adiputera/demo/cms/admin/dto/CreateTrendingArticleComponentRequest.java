package id.adiputera.demo.cms.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTrendingArticleComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> articleIds;
}
