package io.goorm.jpa.entity;

import io.goorm.jpa.entity.common.BaseEntity;
import io.goorm.jpa.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * 수강신청 엔티티
 * - ManyToOne 양방향 (Enrollment ↔ User, Course)
 * - QueryDSL 사용
 * - Optimistic Lock (@Version)
 */
@Entity
@Getter
@ToString(exclude = {"student", "course"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "enrollment",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_no", "course_no"}))
public class Enrollment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long enrollmentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_no", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Version
    private Long version;  // Optimistic Lock

    @Builder
    public Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
        this.status = EnrollmentStatus.PENDING;
    }

    /**
     * 승인
     * - 상태를 APPROVED로 변경
     * - currentStudents 증가
     */
    public void approve() {
        if (this.status == EnrollmentStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 수강신청입니다.");
        }
        this.status = EnrollmentStatus.APPROVED;
        this.course.increaseCurrentStudents();
    }

    /**
     * 거절
     * - 상태를 REJECTED로 변경
     * - currentStudents는 변경하지 않음
     */
    public void reject() {
        if (this.status == EnrollmentStatus.REJECTED) {
            throw new IllegalStateException("이미 거절된 수강신청입니다.");
        }
        this.status = EnrollmentStatus.REJECTED;
    }

    /**
     * 취소
     * - 대기 상태: soft delete만 수행
     * - 승인 상태: soft delete + currentStudents 감소
     */
    public void cancel() {
        if (this.status == EnrollmentStatus.REJECTED) {
            throw new IllegalStateException("거절된 수강신청은 취소할 수 없습니다.");
        }

        // 승인된 상태였다면 currentStudents 감소
        if (this.status == EnrollmentStatus.APPROVED) {
            this.course.decreaseCurrentStudents();
        }

        this.delete();
    }

    /**
     * 대기 상태 확인
     */
    public boolean isPending() {
        return this.status == EnrollmentStatus.PENDING;
    }

    /**
     * 승인 상태 확인
     */
    public boolean isApproved() {
        return this.status == EnrollmentStatus.APPROVED;
    }

    // ===== Step 2: 양방향 관계를 위한 setter 추가 =====
    
    /**
     * Course 설정 (양방향 관계용)
     * - Course.addEnrollment()에서 호출됨
     */
    public void setCourse(Course course) {
        this.course = course;
    }
    
    /**
     * Student 설정 (양방향 관계용)
     * - User.addEnrollment()에서 호출됨
     */
    public void setStudent(User student) {
        this.student = student;
    }
}
