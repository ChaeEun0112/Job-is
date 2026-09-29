package com.leets7th.job_is_be.domain.notification.service;

import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingResponse;
import com.leets7th.job_is_be.domain.notification.dto.NotificationSettingUpdateRequest;
import com.leets7th.job_is_be.domain.notification.dto.SnoozeRequest;
import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.notification.repository.NotificationSettingRepository;
import com.leets7th.job_is_be.domain.user.entity.User;
import com.leets7th.job_is_be.domain.user.repository.UserRepository;
import com.leets7th.job_is_be.global.exception.GeneralException;
import com.leets7th.job_is_be.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    public static final String DEFAULT_SEND_SLOT = "18:30";
    private static final Set<String> ALLOWED_SEND_SLOTS = Set.of(DEFAULT_SEND_SLOT);

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationSettingResponse getSetting(Long userId) {
        return toResponse(getOrCreateSetting(userId));
    }

    @Transactional
    public NotificationSettingResponse updateSetting(Long userId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = getOrCreateSetting(userId);

        if (request.sendSlot() != null) {
            validateSendSlot(request.sendSlot());
            setting.changeSlot(request.sendSlot());
        }
        if (request.briefingEnabled() != null) {
            setting.updateEmailSubscribed(request.briefingEnabled());
        }
        if (request.marketingSubscribed() != null) {
            setting.updateMarketingSubscribed(request.marketingSubscribed());
        }

        return toResponse(setting);
    }

    @Transactional
    public void snooze(Long userId, SnoozeRequest request) {
        if (request.duration() == null) {
            throw new GeneralException(ErrorStatus.NOTIFICATION_SNOOZE_DURATION_REQUIRED);
        }

        NotificationSetting setting = getOrCreateSetting(userId);
        switch (request.duration()) {
            case SEVEN_DAYS -> setting.snooze(LocalDate.now().plusDays(7));
            case THIRTY_DAYS -> setting.snooze(LocalDate.now().plusDays(30));
            case INDEFINITE -> setting.snoozeIndefinitely();
        }
    }

    @Transactional
    public void cancelSnooze(Long userId) {
        getOrCreateSetting(userId).clearSnooze();
    }

    @Transactional
    public NotificationSetting getOrCreateForDelivery(Long userId) {
        return getOrCreateSetting(userId);
    }

    private void validateSendSlot(String sendSlot) {
        if (!ALLOWED_SEND_SLOTS.contains(sendSlot)) {
            throw new GeneralException(ErrorStatus.NOTIFICATION_INVALID_SEND_SLOT);
        }
    }

    private NotificationSetting getOrCreateSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSetting(user));
        if (!DEFAULT_SEND_SLOT.equals(setting.getSendSlot())) {
            setting.changeSlot(DEFAULT_SEND_SLOT);
        }
        return setting;
    }

    // 최초 조회가 동시에 들어오면 findByUser가 둘 다 비어있는 것으로 보고 저장을 시도할 수 있어
    // uk_notification_settings_user 위반이 날 수 있다. saveAndFlush로 즉시 반영해 이 자리에서 잡고,
    // 위반 시 먼저 커밋된 다른 트랜잭션의 행을 재조회한다.
    private NotificationSetting createDefaultSetting(User user) {
        try {
            return notificationSettingRepository.saveAndFlush(
                    NotificationSetting.builder()
                            .user(user)
                            .sendSlot(DEFAULT_SEND_SLOT)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            return notificationSettingRepository.findByUser(user)
                    .orElseThrow(() -> e);
        }
    }

    private NotificationSettingResponse toResponse(NotificationSetting setting) {
        boolean indefinite = setting.isSnoozeIndefinite();
        LocalDate snoozeUntil = setting.getSnoozeUntil();
        // snoozeUntil은 "재개 예정일"이므로 당일 포함해 그 이후는 이미 재개된 것으로 본다.
        boolean dateActive = snoozeUntil != null && snoozeUntil.isAfter(LocalDate.now());
        boolean snoozed = indefinite || dateActive;

        NotificationSettingResponse.SnoozeInfo snoozeInfo = new NotificationSettingResponse.SnoozeInfo(
                snoozed,
                dateActive ? snoozeUntil : null,
                indefinite
        );
        return new NotificationSettingResponse(
                setting.isEmailSubscribed(),
                setting.getSendSlot(),
                setting.isMarketingSubscribed(),
                snoozeInfo
        );
    }
}
