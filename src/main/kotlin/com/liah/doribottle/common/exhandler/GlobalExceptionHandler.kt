package com.liah.doribottle.common.exhandler

import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.common.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
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

    @ExceptionHandler(MissingServletRequestParameterException::class)
    protected fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<FieldErrorResponse> {
        log.error("MissingServletRequestParameterException", e)
        val response = FieldErrorResponse(e.parameterName, e.message, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<FieldErrorResponse> {
        log.error("MethodArgumentNotValidException", e)
        val fieldError = e.bindingResult.fieldError
        val response = fieldError?.let {
            FieldErrorResponse(it.field, it.defaultMessage, HttpStatus.BAD_REQUEST.value())
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
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

    @ExceptionHandler(UnauthorizedException::class)
    protected fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        log.error("UnauthorizedException", e)
        val response = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }
}