package no.nav.syfo.veiledernavn

import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.ws.rs.*


@ControllerAdvice
class VeilederNavnExceptionHandler: ResponseEntityExceptionHandler() {

    private val LOG = LoggerFactory.getLogger(VeilederNavnExceptionHandler::class.java)

    @ExceptionHandler(java.lang.RuntimeException::class)
    protected fun handleRuntimeException(e: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Uventet feil internt i syfoveileder"
        LOG.error(bodyOfResponse, e)
        return handleExceptionInternal(e, bodyOfResponse,
                HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
    }

    @ExceptionHandler(OIDCUnauthorizedException::class)
    protected fun handleUnauthorized(e: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Bruker er ikke logget inn"
        LOG.error(bodyOfResponse, e)
        return handleExceptionInternal(e, bodyOfResponse,
                HttpHeaders(), HttpStatus.UNAUTHORIZED, request)
    }

    @ExceptionHandler(ServiceUnavailableException::class, InternalServerErrorException::class)
    protected fun handleServiceUnavailable(e: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Feil i tjenester som syfoVeileder bruker"
        LOG.error(bodyOfResponse, e)
        return handleExceptionInternal(e, bodyOfResponse,
                HttpHeaders(), HttpStatus.FAILED_DEPENDENCY, request)
    }

    @ExceptionHandler(BadRequestException::class)
    protected fun handleBadRequest(e: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        LOG.warn("Feil i request til GraphApI", e)
        val bodyOfResponse = e.message
        return handleExceptionInternal(e, bodyOfResponse,
                HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }

}