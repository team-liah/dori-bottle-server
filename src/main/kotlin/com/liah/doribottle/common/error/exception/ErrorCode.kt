package com.liah.doribottle.common.error.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(400, "A001", "Invalid input value."),
    METHOD_NOT_ALLOWED(405, "A002", "Invalid input value."),
    INTERNAL_SERVER_ERROR(500, "A003", "Server Error."),
    SMS_SENDING_ERROR(500, "A004", "Sending Sms Error."),
    INVALID_TYPE_VALUE(400, "A005", "Invalid type value."),
    ACCESS_DENIED(403, "A006", "Access is denied."),
    UNAUTHORIZED(401, "A007", "Unauthorized."),

    // Cup
    CUP_DELETE_NOT_ALLOWED(400, "B001", "Cup on loan can not be deleted."),
    CUP_NOT_FOUND(404, "B002", "Cup entity not found."),

    // User
    USER_NOT_FOUND(404, "C001", "User entity not found."),
    USER_ALREADY_REGISTERED(400, "C002", "User is already registered."),
    USER_INVALID_PHONE_NUMBER(400, "C003", "Invalid phone number value."),

    // Machine
}