package io.goorm.jpa.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.goorm.jpa.dto.course.CourseSearchCondition;
import io.goorm.jpa.dto.dashboard.CourseStatistics;
import io.goorm.jpa.entity.Course;
import io.goorm.jpa.entity.QCourse;
import io.goorm.jpa.entity.QEnrollment;
import io.goorm.jpa.entity.QUser;
import io.goorm.jpa.entity.User;
import io.goorm.jpa.enums.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Course QueryDSL Repository
 * - Enrollment 양방향 관계 제거 후: enrollment 테이블과 join으로 통계 조회
 */
@Repository
@RequiredArgsConstructor
public class CourseQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QCourse course = QCourse.course;
    private final QUser user = QUser.user;
    private final QEnrollment enrollment = QEnrollment.enrollment;

    public Page<Course> searchCourses(CourseSearchCondition condition, Pageable pageable) {
        List<Course> content = queryFactory
                .selectFrom(course)
                .leftJoin(course.instructor, user).fetchJoin()
                .where(
                        courseNameContains(condition.getCourseName()),
                        instructorNameContains(condition.getInstructorName()),
                        course.currentStudents.lt(course.maxStudents)
                )
                .orderBy(course.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(course)
                .where(
                        courseNameContains(condition.getCourseName()),
                        instructorNameContains(condition.getInstructorName()),
                        course.currentStudents.lt(course.maxStudents)
                )
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    public Optional<Course> findByIdWithInstructor(Long courseNo) {
        Course result = queryFactory
                .selectFrom(course)
                .leftJoin(course.instructor, user).fetchJoin()
                .where(course.courseNo.eq(courseNo))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public List<Course> findAvailableCourses() {
        return queryFactory
                .selectFrom(course)
                .leftJoin(course.instructor, user).fetchJoin()
                .where(course.currentStudents.lt(course.maxStudents))
                .orderBy(course.createdAt.desc())
                .fetch();
    }

    public List<Course> findByInstructor(User instructor) {
        return queryFactory
                .selectFrom(course)
                .where(course.instructor.eq(instructor))
                .orderBy(course.createdAt.desc())
                .fetch();
    }

    public CourseStatistics getCourseStatistics(Long courseNo) {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo,
                        course.name,
                        course.maxStudents,
                        enrollment.count().as("totalEnrollments"),
                        enrollment.count().as("approvedEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.courseNo.eq(courseNo))
                .groupBy(course.courseNo, course.name, course.maxStudents)
                .fetchOne();
    }

    public List<CourseStatistics> getInstructorStatistics(Long instructorId) {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo,
                        course.name,
                        course.maxStudents,
                        enrollment.count().as("totalEnrollments"),
                        enrollment.count().as("approvedEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.instructor.userNo.eq(instructorId))
                .groupBy(course.courseNo, course.name, course.maxStudents)
                .orderBy(course.createdAt.desc())
                .fetch();
    }

    public List<CourseStatistics> getMonthlyStatistics(int year, int month) {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo,
                        course.name,
                        course.maxStudents,
                        enrollment.count().as("totalEnrollments"),
                        enrollment.count().as("approvedEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(
                        course.createdAt.year().eq(year),
                        course.createdAt.month().eq(month)
                )
                .groupBy(course.courseNo, course.name, course.maxStudents)
                .orderBy(course.createdAt.desc())
                .fetch();
    }

    // ===== CourseService에서 사용하는 추가 메서드들 =====

    /**
     * 강의 전체 통계
     */
    public CourseStatistics getCourseStatistics() {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo.count().as("totalCourses"),
                        course.maxStudents.sum().as("totalCapacity"),
                        enrollment.count().as("totalEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.deleted.eq(false))
                .fetchOne();
    }

    /**
     * 강의별 상세 통계
     */
    public List<CourseStatistics> getCourseDetailedStatistics() {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo,
                        course.name,
                        course.maxStudents,
                        enrollment.count().as("totalEnrollments"),
                        enrollment.countDistinct().as("approvedEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.deleted.eq(false))
                .groupBy(course.courseNo, course.name, course.maxStudents)
                .orderBy(course.createdAt.desc())
                .fetch();
    }

    /**
     * 강사별 강의 통계
     */
    public List<CourseStatistics> getInstructorStatistics() {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.instructor.userNo,
                        course.instructor.fullName,
                        course.courseNo.count().as("totalCourses"),
                        course.maxStudents.sum().as("totalCapacity"),
                        enrollment.count().as("totalEnrollments")
                ))
                .from(course)
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.deleted.eq(false))
                .groupBy(course.instructor.userNo, course.instructor.fullName)
                .orderBy(course.courseNo.count().desc())
                .fetch();
    }

    /**
     * 월별 강의 개설 통계
     */
    public List<CourseStatistics> getMonthlyCourseStatistics() {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.createdAt.year().as("year"),
                        course.createdAt.month().as("month"),
                        course.courseNo.count().as("totalCourses"),
                        course.maxStudents.sum().as("totalCapacity")
                ))
                .from(course)
                .where(course.deleted.eq(false))
                .groupBy(course.createdAt.year(), course.createdAt.month())
                .orderBy(course.createdAt.year().desc(), course.createdAt.month().desc())
                .fetch();
    }

    /**
     * 강의별 커리큘럼 통계
     * - Course ↔ Curriculum은 양방향 유지 (Aggregate 내부)
     */
    public List<CourseStatistics> getCourseCurriculumStatistics() {
        return queryFactory
                .select(Projections.constructor(CourseStatistics.class,
                        course.courseNo,
                        course.name,
                        course.curriculums.size().as("totalCurriculums")
                ))
                .from(course)
                .where(course.deleted.eq(false))
                .orderBy(course.curriculums.size().desc())
                .fetch();
    }

    /**
     * 인기 강의 TOP N
     * - Enrollment와 leftJoin + count로 계산
     */
    public List<Course> findPopularCourses(int limit) {
        return queryFactory
                .selectFrom(course)
                .leftJoin(course.instructor, user).fetchJoin()
                .leftJoin(enrollment).on(
                        enrollment.course.eq(course)
                        .and(enrollment.status.eq(EnrollmentStatus.APPROVED))
                        .and(enrollment.deleted.eq(false))
                )
                .where(course.deleted.eq(false))
                .groupBy(course.courseNo)
                .orderBy(enrollment.count().desc(), course.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 강의 텍스트 검색
     */
    public Page<Course> searchCoursesByText(String searchText, Pageable pageable) {
        List<Course> content = queryFactory
                .selectFrom(course)
                .leftJoin(course.instructor, user).fetchJoin()
                .where(
                        course.name.containsIgnoreCase(searchText)
                        .or(course.description.containsIgnoreCase(searchText))
                        .or(course.instructor.fullName.containsIgnoreCase(searchText))
                )
                .orderBy(course.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(course)
                .where(
                        course.name.containsIgnoreCase(searchText)
                        .or(course.description.containsIgnoreCase(searchText))
                        .or(course.instructor.fullName.containsIgnoreCase(searchText))
                )
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression courseNameContains(String courseName) {
        return courseName != null ? course.name.containsIgnoreCase(courseName) : null;
    }

    private BooleanExpression instructorNameContains(String instructorName) {
        return instructorName != null ? course.instructor.fullName.containsIgnoreCase(instructorName) : null;
    }
}
