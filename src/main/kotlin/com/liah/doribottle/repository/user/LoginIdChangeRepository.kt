package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.LoginIdChange
import org.springframework.data.repository.CrudRepository

interface LoginIdChangeRepository : CrudRepository<LoginIdChange, String>