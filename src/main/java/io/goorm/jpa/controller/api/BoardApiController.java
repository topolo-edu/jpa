package io.goorm.jpa.controller.api;

import io.goorm.jpa.dto.common.ApiResponse;
import io.goorm.jpa.dto.common.PageResponse;
import io.goorm.jpa.dto.board.BoardCreateRequest;
import io.goorm.jpa.dto.board.BoardResponse;
import io.goorm.jpa.dto.board.BoardUpdateRequest;
import io.goorm.jpa.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Board API Controller
 * - Query Methods 사용
 * - ManyToOne 단방향
 * - Step 1: 기본 CRUD + 검색
 */
@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Tag(name = "Board", description = "게시판 관리 API")
public class BoardApiController {

    private final BoardService boardService;

    // ===== 기본 CRUD 엔드포인트 =====

    @GetMapping
    @Operation(summary = "게시글 목록 조회")
    public ApiResponse<PageResponse<BoardResponse>> getList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BoardResponse> page;
        
        // 검색 조건이 있는 경우
        if (title != null || content != null || author != null) {
            page = boardService.searchAll(title, content, author, pageable);
        } else {
            page = boardService.getList(pageable);
        }
        
        return ApiResponse.success(PageResponse.of(page));
    }

    @GetMapping("/search")
    @Operation(summary = "게시글 검색 (개선)", description = "제목, 내용, 작성자 통합 검색 - 정렬 옵션 지원")
    public ApiResponse<PageResponse<BoardResponse>> searchBoards(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String sortBy,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BoardResponse> page = boardService.searchAll(title, content, author, pageable);
        return ApiResponse.success(PageResponse.of(page));
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 게시글 조회 (개선)", description = "조회수 기준 상위 게시글 - 페이징 지원")
    public ApiResponse<List<BoardResponse>> getPopularBoards() {
        List<BoardResponse> boards = boardService.getPopularBoards();
        return ApiResponse.success(boards);
    }

    // ===== 강의 상세 조회 (URL 매핑 충돌 방지를 위해 마지막에 배치) =====
    
    @GetMapping("/{boardNo:[0-9]+}")
    @Operation(summary = "게시글 상세 조회")
    public ApiResponse<BoardResponse> getDetail(@PathVariable Long boardNo) {
        BoardResponse response = boardService.getDetail(boardNo);
        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "게시글 작성")
    public ApiResponse<BoardResponse> create(@Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.create(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{boardNo}")
    @Operation(summary = "게시글 수정 (작성자만)")
    public ApiResponse<BoardResponse> update(
            @PathVariable Long boardNo,
            @Valid @RequestBody BoardUpdateRequest request
    ) {
        BoardResponse response = boardService.update(boardNo, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{boardNo}")
    @Operation(summary = "게시글 삭제 (작성자만)")
    public ApiResponse<Void> delete(@PathVariable Long boardNo) {
        boardService.delete(boardNo);
        return ApiResponse.success();
    }
}