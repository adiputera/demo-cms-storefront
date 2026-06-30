package id.adiputera.demo.cms.admin.service;

import id.adiputera.demo.cms.admin.exception.ResourceNotFoundException;
import id.adiputera.demo.cms.admin.repository.ArticleRepository;
import id.adiputera.demo.cms.admin.repository.CatalogRepository;
import id.adiputera.demo.cms.dto.ArticleDTO;
import id.adiputera.demo.cms.entity.Article;
import id.adiputera.demo.cms.entity.Catalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleManagementService {

    private final ArticleRepository articleRepository;
    private final CatalogRepository catalogRepository;
    private final CatalogSyncService catalogSyncService;

    private Catalog getStagedCatalog() {
        return catalogRepository.findByCatalogIdAndVersion("articleCatalog", id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
                .orElseGet(() -> catalogRepository.save(Catalog.builder()
                        .catalogId("articleCatalog")
                        .version(id.adiputera.demo.cms.entity.CatalogVersion.STAGED)
                        .build()));
    }

    public List<ArticleDTO> getAllArticles() {
        Catalog stagedCatalog = getStagedCatalog();
        List<Article> articles = articleRepository.findAllByCatalog(stagedCatalog, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        java.util.Map<String, String> syncStatusMap = catalogSyncService.calculateSyncStatus(articles, Article.class, "articleCatalog");
        
        return articles.stream().map(article -> {
            ArticleDTO dto = mapToDTO(article);
            dto.setSyncStatus(syncStatusMap.getOrDefault(article.getSyncKey(), "UNKNOWN"));
            return dto;
        }).collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        return mapToDTO(article);
    }

    public ArticleDTO createArticle(ArticleDTO request) {
        Catalog catalog = getStagedCatalog();
        
        Article article = Article.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .body(request.getBody())
                .build();
        article.setCatalog(catalog);
        
        Article savedArticle = articleRepository.save(article);
        return mapToDTO(savedArticle);
    }

    public ArticleDTO updateArticle(Long id, ArticleDTO request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
                
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setBody(request.getBody());
        
        Article updatedArticle = articleRepository.save(article);
        return mapToDTO(updatedArticle);
    }

    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        articleRepository.delete(article);
    }

    private ArticleDTO mapToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .body(article.getBody())
                .build();
    }
}
