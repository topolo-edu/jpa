package io.goorm.jpa.service;

import io.goorm.jpa.dto.board.BoardResponse;
import io.goorm.jpa.dto.dashboard.DashboardResponse;
import io.goorm.jpa.dto.dashboard.PopularCourse;
import io.goorm.jpa.dto.dashboard.PopularCourseCondition;
import io.goorm.jpa.entity.Board;
import io.goorm.jpa.repository.BoardRepository;
import io.goorm.jpa.repository.CourseRepository;
import io.goorm.jpa.repository.EnrollmentRepository;
import io.goorm.jpa.repository.querydsl.EnrollmentQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대시보드 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BoardRepository boardRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentQueryRepository enrollmentQueryRepository;

    /**
     * 대시보드 데이터 조회
     */
    public DashboardResponse getDashboard() {
        // 통계
        Long totalBoards = boardRepository.countByDeletedFalse();
        Long totalCourses = courseRepository.countByDeletedFalse();
        Long totalEnrollments = enrollmentRepository.countByDeletedFalse();

        DashboardResponse.Statistics statistics = new DashboardResponse.Statistics(
                totalBoards,
                totalCourses,
                totalEnrollments
        );

        // 최근 게시글 5개
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Board> recentBoards = boardRepository.findByDeletedFalse(pageRequest).getContent();
        List<BoardResponse> recentBoardResponses = recentBoards.stream()
                .map(BoardResponse::from)
                .toList();

        // 인기 강의 TOP 5 (실제 데이터 조회)
        PopularCourseCondition condition = PopularCourseCondition.of(5);
        List<PopularCourse> popularCoursesData = enrollmentQueryRepository.findPopularCourses(condition);
        
        List<DashboardResponse.PopularCourse> popularCourses = popularCoursesData.stream()
                .map(pc -> new DashboardResponse.PopularCourse(
                        pc.courseNo(),
                        pc.courseName(),
                        pc.instructorName(),
                        pc.studentCount().intValue()
                ))
                .toList();

        return new DashboardResponse(statistics, recentBoardResponses, popularCourses);
    }
}
