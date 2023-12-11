package com.myapp.warmwave.domain.article.service;

import com.myapp.warmwave.common.Role;
import com.myapp.warmwave.domain.article.dto.ArticlePostDto;
import com.myapp.warmwave.domain.article.dto.ArticleResponseDto;
import com.myapp.warmwave.domain.article.entity.Article;
import com.myapp.warmwave.domain.article.entity.ArticleCategory;
import com.myapp.warmwave.domain.article.entity.Status;
import com.myapp.warmwave.domain.article.entity.Type;
import com.myapp.warmwave.domain.article.mapper.ArticleMapper;
import com.myapp.warmwave.domain.article.repository.ArticleCategoryRepository;
import com.myapp.warmwave.domain.article.repository.ArticleRepository;
import com.myapp.warmwave.domain.category.entity.Category;
import com.myapp.warmwave.domain.category.service.CategoryService;
import com.myapp.warmwave.domain.image.entity.Image;
import com.myapp.warmwave.domain.image.service.ImageService;
import com.myapp.warmwave.domain.user.entity.Individual;
import com.myapp.warmwave.domain.user.entity.Institution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private ImageService imageService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ArticleCategoryRepository articleCategoryRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article articleIndiv;
    private Article articleInst;
    private Category category;
    private Image image;
    private ArticleCategory articleCategory;
    private Individual individual;
    private Institution institution;

    @BeforeEach
    void setup() {
        articleIndiv = Article.builder()
                .id(1L).title("제목1").content("내용1").articleType(Type.DONATION)
                .articleStatus(Status.DEFAULT).userIp("123.123.123.123").articleCategories(new ArrayList<>())
                .articleImages(new ArrayList<>()).user(individual).build();

        articleInst = Article.builder()
                .id(2L).title("제목2").content("내용2").articleType(Type.BENEFICIARY)
                .articleStatus(Status.DEFAULT).userIp("111.111.111.111").articleCategories(new ArrayList<>())
                .articleImages(new ArrayList<>()).user(institution).build();

        category = Category.builder()
                .id(1L).name("카테고리1").articleCategories(new ArrayList<>()).build();

        image = Image.builder()
                .id(1L).imgName("이미지1").imgUrl("url1").article(articleIndiv).build();

        articleCategory = ArticleCategory.builder()
                .id(1L).article(articleIndiv).category(category).build();

        individual = Individual.builder()
                .id(1L).role(Role.INDIVIDUAL).build();

        institution = Institution.builder()
                .id(2L).role(Role.INSTITUTION).build();
    }

    @DisplayName("기부글 작성 기능 확인")
    @Test
    void createArticle() throws IOException {
        // given
        ArticlePostDto reqDto = saveArticle();
        List<MultipartFile> imageFiles = new ArrayList<>();

        // when
        Article savedArticle = articleService.createArticle(reqDto, imageFiles);

        // then
        assertThat(savedArticle).isNotNull();
    }

    @DisplayName("기부글 목록 조회 기능 확인")
    @Test
    void readAllArticle() throws IOException {
        // given
        ArticlePostDto reqDto = saveArticle();
        List<MultipartFile> imageFiles = new ArrayList<>();
        articleService.createArticle(reqDto, imageFiles);

        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Article> articleList = List.of(articleIndiv);
        Page<Article> pageList = new PageImpl<>(articleList);

        when(articleRepository.findAll(pageRequest)).thenReturn(pageList);

        // when
        Page<ArticleResponseDto> pageResDtoList = articleService.getAllArticles(1, 5);

        // then
        assertThat(pageResDtoList).hasSameSizeAs(pageList);
    }

    @DisplayName("기부글 조회 기능 확인")
    @Test
    void readArticle() throws IOException {
        // given
        ArticlePostDto reqDto = saveArticle();
        List<MultipartFile> imageFiles = new ArrayList<>();
        Article article = articleService.createArticle(reqDto, imageFiles);

        Long articleId = 1L;

        when(articleRepository.findById(any())).thenReturn(Optional.of(articleIndiv));

        // when
        Article foundArticle = articleService.getArticleByArticleId(articleId);

        // then
        assertThat(foundArticle).isNotNull();
    }

    @DisplayName("기부글 정보 수정 기능 확인")
    @Test
    void updateArticle() throws IOException {
        // given
        ArticlePostDto reqDto = saveArticle();
        List<MultipartFile> imageFiles = new ArrayList<>();
        Article savedArticle = articleService.createArticle(reqDto, imageFiles);
        String title = savedArticle.getTitle();

        Long articleId = 1L;

        when(articleRepository.findById(any())).thenReturn(Optional.of(articleIndiv));

        ArticlePostDto updateDto = ArticlePostDto.builder()
                .title("제목1 변경").content("내용2").prodCategory("카테고리2").build();

        savedArticle.applyPatch(updateDto, List.of(articleCategory));

        // when
        Article foundArticle = articleService.updateArticle(articleId, updateDto, imageFiles);

        // then
        assertThat(foundArticle.getTitle()).isNotEqualTo(title);
    }

    @DisplayName("기부글 삭제 기능 확인")
    @Test
    void deleteArticle() throws IOException {
        // given
        ArticlePostDto reqDto = saveArticle();
        List<MultipartFile> imageFiles = new ArrayList<>();
        Article savedArticle = articleService.createArticle(reqDto, imageFiles);

        Long articleId = 1L;

        when(articleRepository.findById(any())).thenReturn(Optional.of(articleIndiv));

        // when
        articleService.deleteArticle(articleId);

        // then
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).delete(articleIndiv);
    }



    private ArticlePostDto saveArticle() throws IOException {
        ArticlePostDto reqDto = ArticlePostDto.builder()
                .title("제목1").content("내용1").prodCategory("카테고리1").build();

        List<Category> categoryList = List.of(category);
        when(categoryService.getCategory(any())).thenReturn(categoryList);
        when(articleMapper.articlePostDtoToArticle(any())).thenReturn(articleIndiv);
        when(imageService.uploadImages(any(), any())).thenReturn(articleIndiv.getArticleImages());
        when(articleRepository.save(any())).thenReturn(articleIndiv);
        when(articleCategoryRepository.save(any())).thenReturn(articleCategory);
        when(articleCategoryRepository.findByArticleId(anyLong())).thenReturn(articleIndiv.getArticleCategories());

        return reqDto;
    }
}