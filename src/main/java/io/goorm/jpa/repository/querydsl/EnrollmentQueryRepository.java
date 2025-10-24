package io.goorm.jpa.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.goorm.jpa.dto.dashboard.PopularCourse;
import io.goorm.jpa.dto.dashboard.PopularCourseCondition;
import io.goorm.jpa.dto.enrollment.AdminEnrollmentSearchCondition;
import io.goorm.jpa.entity.Enrollment;
import io.goorm.jpa.entity.User;
import io.goorm.jpa.enums.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.goorm.jpa.entity.QEnrollment.enrollment;
import static io.goorm.jpa.entity.QUser.user;
import static io.goorm.jpa.entity.QCourse.course;

/**
 * Enrollment QueryDSL Repository
 * 동적 검색, 집계 쿼리
 */
@Repository
@RequiredArgsConstructor
public class EnrollmentQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 수강신청 동적 검색 (관리자용) - Step 2-3: 공통 조건 모듈 활용
     */
    public Page<Enrollment> searchByAdminConditions(String searchField, String keyword, Boolean include, EnrollmentStatus status, Pageable pageable) {
        List<Enrollment> content = queryFactory
                .selectFrom(enrollment)
                .join(enrollment.student, user).fetchJoin()
                .join(enrollment.course, course).fetchJoin()
                .where(
                    CommonQueryConditions.notDeleted(enrollment.deleted),
                    createSearchCondition(searchField, keyword, include),
                    CommonQueryConditions.enumEq(enrollment.status, status)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(enrollment.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(enrollment.count())
                .from(enrollment)
                .where(
                    CommonQueryConditions.notDeleted(enrollment.deleted),
                    createSearchCondition(searchField, keyword, include),
                    CommonQueryConditions.enumEq(enrollment.status, status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * 내 수강신청 목록 - Step 2-3: 공통 조건 모듈 활용
     */
    public Page<Enrollment> findByStudent(User student, Pageable pageable) {
        List<Enrollment> content = queryFactory
                .selectFrom(enrollment)
                .join(enrollment.course, course).fetchJoin()
                .join(course.instructor, user).fetchJoin()
                .where(
                        enrollment.student.eq(student),
                        CommonQueryConditions.notDeleted(enrollment.deleted)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(enrollment.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(enrollment.count())
                .from(enrollment)
                .where(
                        enrollment.student.eq(student),
                        CommonQueryConditions.notDeleted(enrollment.deleted)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // ===== 관리자용 고급 검색 기능들 =====


    /**
     * 관리자용 수강신청 고급 검색 (DTO 사용)
     */
    public Page<Enrollment> searchByAdminConditions(AdminEnrollmentSearchCondition condition, Pageable pageable) {
        List<Enrollment> content = queryFactory
                .selectFrom(enrollment)
                .join(enrollment.student, user).fetchJoin()
                .join(enrollment.course, course).fetchJoin()
                .join(course.instructor, user).fetchJoin()
                .where(
                        enrollment.deleted.eq(false),
                        studentNameContains(condition.getStudentName()),
                        courseNameContains(condition.getCourseName()),
                        instructorNameContains(condition.getInstructorName()),
                        statusEq(condition.getStatus()),
                        statusIn(condition.getStatusList()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate()),
                        courseCapacityBetween(condition.getMinCapacity(), condition.getMaxCapacity()),
                        enrollmentRatioBetween(condition.getMinEnrollmentRatio(), condition.getMaxEnrollmentRatio())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(createOrderSpecifier(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(enrollment.count())
                .from(enrollment)
                .join(enrollment.student, user)
                .join(enrollment.course, course)
                .join(course.instructor, user)
                .where(
                        enrollment.deleted.eq(false),
                        studentNameContains(condition.getStudentName()),
                        courseNameContains(condition.getCourseName()),
                        instructorNameContains(condition.getInstructorName()),
                        statusEq(condition.getStatus()),
                        statusIn(condition.getStatusList()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate()),
                        courseCapacityBetween(condition.getMinCapacity(), condition.getMaxCapacity()),
                        enrollmentRatioBetween(condition.getMinEnrollmentRatio(), condition.getMaxEnrollmentRatio())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * 수강신청 통계 (상태별, 강의별, 학생별)
     */
    public List<Object[]> getEnrollmentStatistics() {
        return queryFactory
                .select(
                        enrollment.status,
                        enrollment.count(),
                        course.name,
                        user.fullName
                )
                .from(enrollment)
                .join(enrollment.course, course)
                .join(enrollment.student, user)
                .where(enrollment.deleted.eq(false))
                .groupBy(enrollment.status, course.name, user.fullName)
                .orderBy(enrollment.status.asc(), enrollment.count().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                    tuple.get(enrollment.status),
                    tuple.get(enrollment.count()),
                    tuple.get(course.name),
                    tuple.get(user.fullName)
                })
                .toList();
    }

    /**
     * 강의별 수강신청 현황
     */
    public List<Object[]> getCourseEnrollmentStatus() {
        return queryFactory
                .select(
                        course.name,
                        course.instructor.fullName,
                        course.maxStudents,
                        course.currentStudents,
                        enrollment.status,
                        enrollment.count()
                )
                .from(enrollment)
                .join(enrollment.course, course)
                .where(enrollment.deleted.eq(false))
                .groupBy(course.name, course.instructor.fullName, course.maxStudents, course.currentStudents, enrollment.status)
                .orderBy(course.name.asc(), enrollment.status.asc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                    tuple.get(course.name),
                    tuple.get(course.instructor.fullName),
                    tuple.get(course.maxStudents),
                    tuple.get(course.currentStudents),
                    tuple.get(enrollment.status),
                    tuple.get(enrollment.count())
                })
                .toList();
    }

    /**
     * 학생별 수강신청 현황
     */
    public List<Object[]> getStudentEnrollmentStatus() {
        return queryFactory
                .select(
                        user.fullName,
                        user.username,
                        enrollment.status,
                        enrollment.count()
                )
                .from(enrollment)
                .join(enrollment.student, user)
                .where(enrollment.deleted.eq(false))
                .groupBy(user.fullName, user.username, enrollment.status)
                .orderBy(user.fullName.asc(), enrollment.status.asc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                    tuple.get(user.fullName),
                    tuple.get(user.username),
                    tuple.get(enrollment.status),
                    tuple.get(enrollment.count())
                })
                .toList();
    }

    // ===== Step 2-3: 공통 조건 모듈 활용 =====

    /**
     * 검색 조건 생성 (공통 조건 모듈 활용)
     */
    private BooleanExpression createSearchCondition(String searchField, String keyword, Boolean include) {
        if (searchField == null || keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        String searchKeyword = keyword.trim();
        
        return switch (searchField) {
            case "studentName" -> CommonQueryConditions.stringContains(
                enrollment.student.fullName, searchKeyword, include);
            case "courseName" -> CommonQueryConditions.stringContains(
                enrollment.course.name, searchKeyword, include);
            case "instructorName" -> CommonQueryConditions.stringContains(
                enrollment.course.instructor.fullName, searchKeyword, include);
            case "all" -> textSearch(searchKeyword);
            default -> textSearch(searchKeyword);
        };
    }

    /**
     * 텍스트 검색 (전체 필드)
     */
    private BooleanExpression textSearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) return null;
        
        String trimmedText = searchText.trim();
        return enrollment.student.fullName.contains(trimmedText)
            .or(enrollment.course.name.contains(trimmedText))
            .or(enrollment.course.instructor.fullName.contains(trimmedText));
    }

    // ===== 기존 동적 조건 메서드들 (공통 조건 모듈 활용) =====

    private BooleanExpression studentNameContains(String studentName) {
        return CommonQueryConditions.stringContains(enrollment.student.fullName, studentName);
    }
    
    private BooleanExpression studentNameContains(String studentName, Boolean include) {
        return CommonQueryConditions.stringContains(enrollment.student.fullName, studentName, include);
    }

    private BooleanExpression courseNameContains(String courseName) {
        return CommonQueryConditions.stringContains(enrollment.course.name, courseName);
    }
    
    private BooleanExpression courseNameContains(String courseName, Boolean include) {
        return CommonQueryConditions.stringContains(enrollment.course.name, courseName, include);
    }

    private BooleanExpression instructorNameContains(String instructorName) {
        return CommonQueryConditions.stringContains(enrollment.course.instructor.fullName, instructorName);
    }
    
    private BooleanExpression instructorNameContains(String instructorName, Boolean include) {
        return CommonQueryConditions.stringContains(enrollment.course.instructor.fullName, instructorName, include);
    }

    private BooleanExpression statusIn(List<EnrollmentStatus> statusList) {
        return CommonQueryConditions.enumIn(enrollment.status, statusList);
    }

    private BooleanExpression createdAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return CommonQueryConditions.dateBetween(enrollment.createdAt, startDate, endDate);
    }

    private BooleanExpression courseCapacityBetween(Integer minCapacity, Integer maxCapacity) {
        return CommonQueryConditions.numberBetween(enrollment.course.maxStudents, minCapacity, maxCapacity);
    }

    private BooleanExpression enrollmentRatioBetween(Double minRatio, Double maxRatio) {
        return CommonQueryConditions.ratioBetween(
            enrollment.course.currentStudents, enrollment.course.maxStudents, minRatio, maxRatio);
    }

    private com.querydsl.core.types.OrderSpecifier<?> createOrderSpecifier(org.springframework.data.domain.Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return enrollment.createdAt.desc();
        }
        
        // Sort의 첫 번째 Order를 사용
        org.springframework.data.domain.Sort.Order firstOrder = sort.iterator().next();
        String property = firstOrder.getProperty();
        org.springframework.data.domain.Sort.Direction direction = firstOrder.getDirection();
        
        return switch (property) {
            case "createdAt" -> direction.isAscending() ? enrollment.createdAt.asc() : enrollment.createdAt.desc();
            case "status" -> direction.isAscending() ? enrollment.status.asc() : enrollment.status.desc();
            case "studentName" -> direction.isAscending() ? enrollment.student.fullName.asc() : enrollment.student.fullName.desc();
            case "courseName" -> direction.isAscending() ? enrollment.course.name.asc() : enrollment.course.name.desc();
            case "instructorName" -> direction.isAscending() ? enrollment.course.instructor.fullName.asc() : enrollment.course.instructor.fullName.desc();
            default -> enrollment.createdAt.desc();
        };
    }
    

    /**
     * 동적 조건: 강의 번호
     */
    private BooleanExpression courseNoEq(Long courseNo) {
        return courseNo != null ? enrollment.course.courseNo.eq(courseNo) : null;
    }

    /**
     * 동적 조건: 상태
     */
    private BooleanExpression statusEq(EnrollmentStatus status) {
        return status != null ? enrollment.status.eq(status) : null;
    }

    /**
     * 인기 강의 조회 (수강생 수 기준)
     */
    public List<PopularCourse> findPopularCourses(PopularCourseCondition condition) {
        return queryFactory
            .select(Projections.constructor(PopularCourse.class,
                course.courseNo,
                course.name,
                course.instructor.fullName,
                enrollment.count()
            ))
            .from(course)
            .leftJoin(enrollment).on(
                course.courseNo.eq(enrollment.course.courseNo)
                .and(enrollment.status.eq(EnrollmentStatus.APPROVED))
                .and(enrollment.deleted.eq(false))
            )
            .where(
                course.deleted.eq(false),
                instructorNameContainsForPopular(condition.getInstructorName()),
                minStudentCount(condition.getMinStudentCount()),
                maxStudentCount(condition.getMaxStudentCount())
            )
            .groupBy(course.courseNo, course.name, course.instructor.fullName)
            .orderBy(enrollment.count().desc())
            .limit(condition.getLimit())
            .fetch();
    }

    /**
     * 동적 조건: 강사명 포함 (인기강좌용)
     */
    private BooleanExpression instructorNameContainsForPopular(String instructorName) {
        return instructorName != null ? course.instructor.fullName.contains(instructorName) : null;
    }

    /**
     * 동적 조건: 최소 수강생 수
     */
    private BooleanExpression minStudentCount(Long minStudentCount) {
        return minStudentCount != null ? enrollment.count().goe(minStudentCount) : null;
    }

    /**
     * 동적 조건: 최대 수강생 수
     */
    private BooleanExpression maxStudentCount(Long maxStudentCount) {
        return maxStudentCount != null ? enrollment.count().loe(maxStudentCount) : null;
    }
}
