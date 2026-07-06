package id.adiputera.demo.cms.admin.controller;

import id.adiputera.demo.cms.admin.dto.ApiResponse;
import id.adiputera.demo.cms.admin.dto.CreateArticleRequest;
import id.adiputera.demo.cms.admin.service.ArticleManagementService;
import id.adiputera.demo.cms.dto.ArticleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Article Management Controller class.
 *
 * @author Yusuf F. Adiputera
 */
@RestController
@RequestMapping("/api/cms/articles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ArticleManagementController {

    private final ArticleManagementService articleManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getAllArticles() {
        log.info("GET /api/cms/articles");
        List<ArticleDTO> articles = articleManagementService.getAllArticles();
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(@PathVariable("id") Long id) {
        log.info("GET /api/cms/articles/{}", id);
        ArticleDTO article = articleManagementService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        log.info("POST /api/cms/articles - Creating article: {}", request.getTitle());
        
        ArticleDTO articleDTO = ArticleDTO.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .body(request.getBody())
                .build();

        ArticleDTO createdArticle = articleManagementService.createArticle(articleDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", createdArticle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateArticleRequest request) {
        log.info("PUT /api/cms/articles/{}", id);
        
        ArticleDTO articleDTO = ArticleDTO.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .body(request.getBody())
                .build();

        ArticleDTO updatedArticle = articleManagementService.updateArticle(id, articleDTO);
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", updatedArticle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable("id") Long id) {
        log.info("DELETE /api/cms/articles/{}", id);
        articleManagementService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }
}
