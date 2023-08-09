package com.liah.doribottle.repository.notification

import com.liah.doribottle.domain.notification.Alert
import org.springframework.data.repository.CrudRepository

interface AlertRepository : CrudRepository<Alert, String>