package com.liah.doribottle.common.exhandler

import com.liah.doribottle.common.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice(annotations = [RestController::class])
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Exception", e)
        val response = ErrorResponse("Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value())
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(SmsSendingException::class)
    protected fun handleSmsSendingException(e: SmsSendingException): ResponseEntity<ErrorResponse> {
        log.error("SmsSendingException", e)
        val response = ErrorResponse(e.message, HttpStatus.INTERNAL_SERVER_ERROR.value())
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    protected fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.error("IllegalArgumentException", e)
        val response = ErrorResponse(e.message, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        log.error("HttpRequestMethodNotSupportedException", e)
        val response = ErrorResponse(e.message, HttpStatus.METHOD_NOT_ALLOWED.value())
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.error("HttpMessageNotReadableException", e)
        val response = ErrorResponse(e.message, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentNotValidException", e)
        val fieldError = e.bindingResult.fieldError
        val response = fieldError?.let {
            ErrorResponse("${it.field} - ${it.defaultMessage}", HttpStatus.BAD_REQUEST.value())
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BadCredentialsException::class)
    protected fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        log.error("BadCredentialsException", e)
        val response = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(DisabledException::class)
    protected fun handleDisabledException(e: DisabledException): ResponseEntity<ErrorResponse> {
        log.error("DisabledException", e)
        val response = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(LockedException::class)
    protected fun handleLockedException(e: LockedException): ResponseEntity<ErrorResponse> {
        log.error("LockedException", e)
        val response = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    /**
     * Custom Exception
     */

    @ExceptionHandler(NotFoundException::class)
    protected fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        log.error("NotFoundException", e)
        val response = ErrorResponse(e.message, HttpStatus.NOT_FOUND.value())
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(BadRequestException::class)
    protected fun handleBadRequestException(e: BadRequestException): ResponseEntity<ErrorResponse> {
        log.error("BadRequestException", e)
        val response = ErrorResponse(e.message, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(UnauthorizedException::class)
    protected fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        log.error("UnauthorizedException", e)
        val response = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(ForbiddenException::class)
    protected fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        log.error("ForbiddenException", e)
        val response = ErrorResponse(e.message, HttpStatus.FORBIDDEN.value())
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }
}