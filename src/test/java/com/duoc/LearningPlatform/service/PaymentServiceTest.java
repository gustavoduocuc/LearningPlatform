package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentMethod;
import com.duoc.LearningPlatform.model.PaymentStatus;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.PaymentRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final long COURSE_ID = 1L;
    private static final long STUDENT_ID = 2L;
    private static final long OTHER_STUDENT_ID = 3L;
    private static final long PAYMENT_ID = 5L;
    private static final long ADMIN_ACTOR_ID = 99L;
    private static final long NON_STUDENT_USER_ID = 1L;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void simulatedPaymentIsApprovedWithReferenceWhenStudentIsRegisteredForCourse() {
        User student = studentWithId(STUDENT_ID);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseService.getCourse(COURSE_ID)).thenReturn(sampleCourse());
        when(registrationService.isRegistered(COURSE_ID, STUDENT_ID)).thenReturn(true);
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(STUDENT_ID, COURSE_ID, PaymentStatus.APPROVED))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            assignEntityId(saved, 99L);
            return saved;
        });

        Payment result = paymentService.createSimulatedPayment(
                STUDENT_ID, COURSE_ID, new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD);

        assertEquals(99L, result.getId());
        assertEquals(PaymentStatus.APPROVED, result.getPaymentStatus());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertNotNull(result.getTransactionReference());
        assertFalse(result.getTransactionReference().isBlank());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void rejectsSimulatedPaymentWhenCourseDoesNotExist() {
        User student = studentWithId(STUDENT_ID);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseService.getCourse(COURSE_ID)).thenThrow(new ResourceNotFoundException("Course", COURSE_ID));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createSimulatedPayment(
                        STUDENT_ID, COURSE_ID, new BigDecimal("50.00"), PaymentMethod.BANK_TRANSFER));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsSimulatedPaymentWhenStudentUserNotFound() {
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createSimulatedPayment(
                        STUDENT_ID, COURSE_ID, new BigDecimal("50.00"), PaymentMethod.CREDIT_CARD));

        assertTrue(ex.getMessage().contains("Student"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsSimulatedPaymentWhenPayerDoesNotHaveStudentRole() {
        User admin = userWithIdAndRole(NON_STUDENT_USER_ID, Role.ADMIN);
        when(userRepository.findById(NON_STUDENT_USER_ID)).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createSimulatedPayment(
                        NON_STUDENT_USER_ID, COURSE_ID, new BigDecimal("50.00"), PaymentMethod.CREDIT_CARD));

        assertTrue(ex.getMessage().contains("student"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsSimulatedPaymentWhenStudentNotRegisteredForCourse() {
        User student = studentWithId(STUDENT_ID);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseService.getCourse(COURSE_ID)).thenReturn(sampleCourse());
        when(registrationService.isRegistered(COURSE_ID, STUDENT_ID)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> paymentService.createSimulatedPayment(
                        STUDENT_ID, COURSE_ID, new BigDecimal("50.00"), PaymentMethod.CREDIT_CARD));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsSimulatedPaymentWhenApprovedPaymentAlreadyExistsForSameEnrollment() {
        User student = studentWithId(STUDENT_ID);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseService.getCourse(COURSE_ID)).thenReturn(sampleCourse());
        when(registrationService.isRegistered(COURSE_ID, STUDENT_ID)).thenReturn(true);
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(STUDENT_ID, COURSE_ID, PaymentStatus.APPROVED))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> paymentService.createSimulatedPayment(
                        STUDENT_ID, COURSE_ID, new BigDecimal("50.00"), PaymentMethod.CREDIT_CARD));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void rejectsSimulatedPaymentWhenAmountIsNotPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createSimulatedPayment(
                        STUDENT_ID, COURSE_ID, BigDecimal.ZERO, PaymentMethod.CREDIT_CARD));

        verify(userRepository, never()).findById(anyLong());
        verify(courseService, never()).getCourse(anyLong());
    }

    @Test
    void listsAllPaymentsForAdminActor() {
        Payment first = approvedPayment(1L, 10L, new BigDecimal("10"), "ref1");
        Payment second = approvedPayment(2L, 11L, new BigDecimal("20"), "ref2");
        assignEntityId(first, 1L);
        assignEntityId(second, 2L);
        when(paymentRepository.findAll()).thenReturn(List.of(first, second));

        List<Payment> result = paymentService.findPaymentsForActor(ADMIN_ACTOR_ID, Role.ADMIN);

        assertEquals(2, result.size());
        verify(paymentRepository).findAll();
        verify(paymentRepository, never()).findByStudentId(any());
    }

    @Test
    void listsOnlyOwnPaymentsForStudentActor() {
        Payment own = approvedPayment(STUDENT_ID, 10L, new BigDecimal("10"), "ref1");
        when(paymentRepository.findByStudentId(STUDENT_ID)).thenReturn(List.of(own));

        List<Payment> result = paymentService.findPaymentsForActor(STUDENT_ID, Role.STUDENT);

        assertEquals(1, result.size());
        verify(paymentRepository).findByStudentId(STUDENT_ID);
        verify(paymentRepository, never()).findAll();
    }

    @Test
    void returnsAnyPaymentByIdForAdminActor() {
        Payment stored = approvedPayment(STUDENT_ID, 10L, new BigDecimal("10"), "ref1");
        assignEntityId(stored, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(stored));

        Payment result = paymentService.getPaymentForActor(PAYMENT_ID, NON_STUDENT_USER_ID, Role.ADMIN);

        assertEquals(PAYMENT_ID, result.getId());
    }

    @Test
    void returnsOwnPaymentByIdForStudentActor() {
        Payment stored = approvedPayment(STUDENT_ID, 10L, new BigDecimal("10"), "ref1");
        assignEntityId(stored, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(stored));

        Payment result = paymentService.getPaymentForActor(PAYMENT_ID, STUDENT_ID, Role.STUDENT);

        assertEquals(PAYMENT_ID, result.getId());
    }

    @Test
    void deniesStudentAccessToAnotherStudentsPayment() {
        Payment stored = approvedPayment(OTHER_STUDENT_ID, 10L, new BigDecimal("10"), "ref1");
        assignEntityId(stored, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(stored));

        assertThrows(AccessDeniedException.class,
                () -> paymentService.getPaymentForActor(PAYMENT_ID, STUDENT_ID, Role.STUDENT));
    }

    @Test
    void failsToGetPaymentWhenIdDoesNotExist() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentForActor(PAYMENT_ID, ADMIN_ACTOR_ID, Role.ADMIN));
    }

    @Test
    void updatesPaymentStatusFromPendingToApproved() {
        Payment pending = paymentWithStatus(STUDENT_ID, 10L, PaymentStatus.PENDING, "ref1");
        assignEntityId(pending, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(pending));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment updated = paymentService.updateStatus(PAYMENT_ID, PaymentStatus.APPROVED);

        assertEquals(PaymentStatus.APPROVED, updated.getPaymentStatus());
        verify(paymentRepository).save(pending);
    }

    @Test
    void rejectsInvalidStatusTransitionFromRejected() {
        Payment rejected = paymentWithStatus(STUDENT_ID, 10L, PaymentStatus.REJECTED, "ref1");
        assignEntityId(rejected, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(rejected));

        assertThrows(IllegalStateException.class,
                () -> paymentService.updateStatus(PAYMENT_ID, PaymentStatus.APPROVED));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void cancelsPaymentFromApprovedState() {
        Payment approved = paymentWithStatus(STUDENT_ID, 10L, PaymentStatus.APPROVED, "ref1");
        assignEntityId(approved, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(approved));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment updated = paymentService.cancelPayment(PAYMENT_ID);

        assertEquals(PaymentStatus.CANCELLED, updated.getPaymentStatus());
    }

    @Test
    void rejectsCancelWhenPaymentAlreadyCancelled() {
        Payment cancelled = paymentWithStatus(STUDENT_ID, 10L, PaymentStatus.CANCELLED, "ref1");
        assignEntityId(cancelled, PAYMENT_ID);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(cancelled));

        assertThrows(IllegalStateException.class, () -> paymentService.cancelPayment(PAYMENT_ID));
    }

    @Test
    void persistsAmountMethodAndApprovedStatusWhenSavingSimulatedPayment() {
        User student = studentWithId(STUDENT_ID);
        when(userRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseService.getCourse(COURSE_ID)).thenReturn(sampleCourse());
        when(registrationService.isRegistered(COURSE_ID, STUDENT_ID)).thenReturn(true);
        when(paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(STUDENT_ID, COURSE_ID, PaymentStatus.APPROVED))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createSimulatedPayment(
                STUDENT_ID, COURSE_ID, new BigDecimal("75.50"), PaymentMethod.BANK_TRANSFER);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertEquals(new BigDecimal("75.50"), saved.getAmount());
        assertEquals(PaymentMethod.BANK_TRANSFER, saved.getPaymentMethod());
        assertEquals(PaymentStatus.APPROVED, saved.getPaymentStatus());
    }

    private static User studentWithId(long id) {
        User user = new User("John", "john@example.com", "password", Role.STUDENT);
        assignEntityId(user, id);
        return user;
    }

    private static User userWithIdAndRole(long id, Role role) {
        User user = new User("Actor", "actor@example.com", "password", role);
        assignEntityId(user, id);
        return user;
    }

    private static Course sampleCourse() {
        return new Course("Java", "Desc", 10L);
    }

    private static Payment approvedPayment(long studentId, long courseId, BigDecimal amount, String reference) {
        return new Payment(studentId, courseId, amount, PaymentMethod.CREDIT_CARD, PaymentStatus.APPROVED, reference);
    }

    private static Payment paymentWithStatus(long studentId, long courseId, PaymentStatus status, String reference) {
        return new Payment(studentId, courseId, new BigDecimal("10"), PaymentMethod.CREDIT_CARD, status, reference);
    }

    private static void assignEntityId(Object entity, long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
