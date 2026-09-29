package com.leets7th.job_is_be.domain.notification.repository;

import com.leets7th.job_is_be.domain.notification.entity.NotificationSetting;
import com.leets7th.job_is_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUser(User user);

    Optional<NotificationSetting> findByUnsubscribeToken(String unsubscribeToken);

    @Query("select ns from NotificationSetting ns join fetch ns.user u "
            + "where ns.emailSubscribed = true and u.status = com.leets7th.job_is_be.domain.user.enums.UserStatus.ACTIVE")
    List<NotificationSetting> findAllActiveEmailSubscribers();
}
