package com.liah.doribottle.web.admin.user

import com.liah.doribottle.service.user.UserService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api/user")
class UserResource(
    private val userService: UserService
) {

}