package id.adiputera.demo.cms.admin.repository;

import id.adiputera.demo.cms.entity.Article;
import id.adiputera.demo.cms.entity.Catalog;

import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends CatalogAwareRepository<Article> {
}
