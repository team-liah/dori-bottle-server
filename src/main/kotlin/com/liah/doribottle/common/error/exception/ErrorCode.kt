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
    CUP_RETURN_NOT_ALLOWED(400, "B003", "Cup in non-returnable state."),
    CUP_LOAN_NOT_ALLOWED(400, "B004å", "Cup in non-loanable state."),

    // User
    USER_NOT_FOUND(404, "C001", "User entity not found."),
    USER_ALREADY_REGISTERED(400, "C002", "User is already registered."),

    // Machine
    MACHINE_NOT_FOUND(404, "D001", "Machine entity not found."),
    LACK_OF_CUP(400, "E003", "Lack of cups in machine."),
    FULL_OF_CUP(400, "E004", "Full of cups in machine."),

    // Rental
    RENTAL_NOT_FOUND(404, "E001", "Rental entity not found."),
    LACK_OF_POINT(400, "E002", "Lack of points to rent cup.")
}