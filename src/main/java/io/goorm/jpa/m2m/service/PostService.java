package io.goorm.jpa.m2m.service;

import io.goorm.jpa.m2m.dto.PostTagResponse;
import io.goorm.jpa.m2m.entity.Post;
import io.goorm.jpa.m2m.entity.PostTag;
import io.goorm.jpa.m2m.entity.Tag;
import io.goorm.jpa.m2m.repository.PostRepository;
import io.goorm.jpa.m2m.repository.PostTagRepository;
import io.goorm.jpa.m2m.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    /**
     * 게시글에 태그 추가
     */
    @Transactional
    public void addTagToPost(Long postId, String tagName) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 태그 조회 또는 생성
        Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));

        // 3. 중복 체크
        if (postTagRepository.existsByPostIdAndTagId(postId, tag.getId())) {
            throw new IllegalStateException("이미 추가된 태그입니다.");
        }

        // 4. 중간 엔티티 생성 및 저장
        PostTag postTag = new PostTag(post, tag);
        postTagRepository.save(postTag);
    }

    /**
     * 게시글에서 태그 제거
     */
    @Transactional
    public void removeTagFromPost(Long postId, Long tagId) {
        // 중간 테이블에서 직접 삭제
        postTagRepository.deleteByPostIdAndTagId(postId, tagId);
    }

    /**
     * 태그 순서 변경
     */
    @Transactional
    public void updateTagOrder(Long postId, Long tagId, Integer newOrder) {
        List<PostTag> postTags = postTagRepository.findByPostId(postId);

        PostTag targetPostTag = postTags.stream()
                .filter(pt -> pt.getTag().getId().equals(tagId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 태그를 찾을 수 없습니다."));

        targetPostTag.updateOrder(newOrder);
    }

    /**
     * 모든 게시글 조회
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 특정 게시글의 모든 태그 조회 (DTO로 변환)
     */
    public List<PostTagResponse> getPostTags(Long postId) {
        List<PostTag> postTags = postTagRepository.findByPostId(postId);

        // Entity → DTO 변환
        return postTags.stream()
                .map(PostTagResponse::from)
                .toList();
    }
}
