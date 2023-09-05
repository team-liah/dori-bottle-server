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
    SMS_SENDING_ERROR(500, "A004", "Sms Sending Error."),
    INVALID_TYPE_VALUE(400, "A005", "Invalid type value."),
    ACCESS_DENIED(403, "A006", "Access is denied."),
    UNAUTHORIZED(401, "A007", "Unauthorized."),
    BILLING_KEY_ISSUANCE_ERROR(500, "A008", "Billing Key Issuance Error."),
    BILLING_EXECUTE_ERROR(500, "A009", "Billing Execute Error."),
    PAYMENT_CANCEL_ERROR(500, "A010", "Payment Cancel Error."),

    // Cup
    CUP_DELETE_NOT_ALLOWED(400, "B001", "Cup on loan can not be deleted."),
    CUP_NOT_FOUND(404, "B002", "Cup entity not found."),
    CUP_LOAN_NOT_ALLOWED(400, "B004", "Cup in non-loanable state."),
    CUP_ALREADY_REGISTERED(400, "B005", "Cup is already registered."),

    // User
    USER_NOT_FOUND(404, "C001", "User entity not found."),
    USER_ALREADY_REGISTERED(400, "C002", "User is already registered."),
    INVITER_ALREADY_REGISTERED(400, "C003", "Inviter is already registered."),
    INVITER_NOT_ALLOWED(400, "C004", "Invalid inviter."),
    INVITER_REGISTRATION_OVERDUE(400, "C005", "Inviter registration is overdue."),
    INVITER_NOT_FOUND(404, "C006", "Inviter entity not found."),
    BLOCKED_USER_ACCESS_DENIED(403, "C007", "BlockedUser is denied access."),
    UNBLOCKED_USER(400, "C008", "Unblocked user."),

    // Machine
    MACHINE_NOT_FOUND(404, "D001", "Machine entity not found."),
    LACK_OF_CUP(400, "D002", "Lack of cups in machine."),
    FULL_OF_CUP(400, "D003", "Full of cups in machine."),
    MACHINE_ALREADY_REGISTERED(400, "D004", "Machine is already registered."),

    // Rental
    RENTAL_NOT_FOUND(404, "E001", "Rental entity not found."),
    LACK_OF_POINT(400, "E002", "Lack of points to rent cup."),

    // Group
    GROUP_NOT_FOUND(404, "F001", "Group entity not found."),
    GROUP_NOT_MEMBER(400, "F002", "User entity not a member of Group."),

    // Payment
    PAYMENT_CATEGORY_NOT_FOUND(404, "G001", "Payment Category entity not found."),
    PAYMENT_METHOD_NOT_FOUND(404, "G002", "Payment Method entity not found."),
    PAYMENT_METHOD_REMOVE_NOT_ALLOWED(400, "G003", "Default Payment Method is can't removed."),
    PAYMENT_NOT_FOUND(404, "G004", "Payment entity not found."),
    PAYMENT_CANCEL_NOT_ALLOWED(400, "G005", "Payment can not be canceled."),

    // Notification
    NOTIFICATION_NOT_FOUNT(404, "H001", "Notification entity not found."),

    // Point
    POINT_NOT_FOUNT(404, "I001", "Point entity not found."),
}