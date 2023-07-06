package com.liah.doribottle.common.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(400, "1001", "Invalid input value."),
    METHOD_NOT_ALLOWED(405, "1002", "Invalid input value."),
    INTERNAL_SERVER_ERROR(500, "1004", "Server Error."),
    INVALID_TYPE_VALUE(400, "1005", "Invalid type value."),
    ACCESS_DENIED(403, "1006", "Access is denied."),
    UNAUTHORIZED(401, "1007", "Unauthorized"),

    // cup
    CUP_DELETE_NOT_ALLOWED(400, "2001", "Cup on loan can not be deleted."),
    CUP_NOT_FOUND(404, "2002", "Cup entity not found."),

    // user
    USER_NOT_FOUND(404, "3001", "User entity not found."),
    USER_ALREADY_REGISTERED(400, "3002", "User is already registered.")
}