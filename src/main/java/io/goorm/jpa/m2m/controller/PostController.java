package io.goorm.jpa.m2m.controller;

import io.goorm.jpa.m2m.dto.PostTagResponse;
import io.goorm.jpa.m2m.entity.Post;
import io.goorm.jpa.m2m.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 1. 모든 게시글 조회
     */
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    /**
     * 2. 특정 게시글의 태그 목록 조회 (DTO 반환)
     */
    @GetMapping("/{postId}/tags")
    public List<PostTagResponse> getPostTags(@PathVariable Long postId) {
        return postService.getPostTags(postId);
    }

    /**
     * 3. 게시글에 태그 추가
     */
    @PostMapping("/{postId}/tags")
    public String addTag(
            @PathVariable Long postId,
            @RequestParam String tagName
    ) {
        postService.addTagToPost(postId, tagName);
        return "태그 추가 완료: " + tagName;
    }

    /**
     * 4. 게시글에서 태그 제거
     */
    @DeleteMapping("/{postId}/tags/{tagId}")
    public String removeTag(
            @PathVariable Long postId,
            @PathVariable Long tagId
    ) {
        postService.removeTagFromPost(postId, tagId);
        return "태그 제거 완료";
    }

    /**
     * 5. 태그 순서 변경 (추가 필드 활용)
     */
    @PatchMapping("/{postId}/tags/{tagId}/order")
    public String updateTagOrder(
            @PathVariable Long postId,
            @PathVariable Long tagId,
            @RequestParam Integer order
    ) {
        postService.updateTagOrder(postId, tagId, order);
        return "태그 순서 변경 완료: " + order;
    }
}
