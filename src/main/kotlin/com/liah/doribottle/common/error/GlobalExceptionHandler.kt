package com.liah.doribottle.common.error

import com.liah.doribottle.common.error.exception.*
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.extension.expireCookie
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException


@RestControllerAdvice(annotations = [RestController::class])
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Exception", e)
        val response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(BindException::class)
    protected fun handleBindException(e: BindException): ResponseEntity<ErrorResponse> {
        log.error("BindException", e)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentTypeMismatchException", e)
        val response = ErrorResponse.of(e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        log.error("HttpRequestMethodNotSupportedException", e)
        val response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.error("HttpMessageNotReadableException", e)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentNotValidException", e)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(BadCredentialsException::class)
    protected fun handleBadCredentialsException(httpRequest: HttpServletRequest, e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        log.error("BadCredentialsException", e)
        val response = ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        val expiredAccessTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .body(response)
    }

    @ExceptionHandler(DisabledException::class)
    protected fun handleDisabledException(httpRequest: HttpServletRequest, e: DisabledException): ResponseEntity<ErrorResponse> {
        log.error("DisabledException", e)
        val response = ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        val expiredAccessTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .body(response)
    }

    @ExceptionHandler(LockedException::class)
    protected fun handleLockedException(httpRequest: HttpServletRequest, e: LockedException): ResponseEntity<ErrorResponse> {
        log.error("LockedException", e)
        val response = ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        val expiredAccessTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .body(response)
    }

    /**
     * Custom Exception
     */

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error("BusinessException", e)
        val response = ErrorResponse.of(e.errorCode)
        return ResponseEntity.status(HttpStatus.valueOf(response.status)).body(response)
    }

    @ExceptionHandler(NotFoundException::class)
    protected fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        log.error("NotFoundException", e)
        val response = ErrorResponse.of(e.errorCode)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(BadRequestException::class)
    protected fun handleBadRequestException(e: BadRequestException): ResponseEntity<ErrorResponse> {
        log.error("BadRequestException", e)
        val response = ErrorResponse.of(e.errorCode)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(UnauthorizedException::class)
    protected fun handleUnauthorizedException(httpRequest: HttpServletRequest, e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        log.error("UnauthorizedException", e)
        val response = ErrorResponse.of(e.errorCode)
        val expiredAccessTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .body(response)
    }

    @ExceptionHandler(ForbiddenException::class)
    protected fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        log.error("ForbiddenException", e)
        val response = ErrorResponse.of(e.errorCode)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }
}