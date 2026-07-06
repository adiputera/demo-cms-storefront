package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Article;
import org.springframework.stereotype.Repository;

/**
 * Article Repository interface.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface ArticleRepository extends CatalogAwareRepository<Article> {
}
