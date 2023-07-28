package com.liah.doribottle.repository.notification

import com.liah.doribottle.domain.notification.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID>