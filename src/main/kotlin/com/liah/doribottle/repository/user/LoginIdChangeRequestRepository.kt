package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.LoginIdChangeRequest
import org.springframework.data.repository.CrudRepository

interface LoginIdChangeRequestRepository : CrudRepository<LoginIdChangeRequest, String>