package br.com.AllTallent.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandleKnownExceptions() {
        assertThat(handler.handleResourceNotFound(new ResourceNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleUnauthorizedAction(new UnauthorizedActionException("forbidden")).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(handler.handleEntityNotFound(new EntityNotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleBadCredentials(new org.springframework.security.authentication.BadCredentialsException("bad"))
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldHandleValidationMessagesWithAndWithoutDefaultMessage() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "email", "Email inválido"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        assertThat(handler.handleValidation(exception).getBody()).isEqualTo("Email inválido");

        BeanPropertyBindingResult nullMessageResult = new BeanPropertyBindingResult(new Object(), "obj");
        nullMessageResult.addError(new FieldError("obj", "email", null, false, null, null, null));
        MethodArgumentNotValidException nullMessageException =
                new MethodArgumentNotValidException((MethodParameter) null, nullMessageResult);

        assertThat(handler.handleValidation(nullMessageException).getBody()).isEqualTo("Requisição inválida.");

        BeanPropertyBindingResult emptyResult = new BeanPropertyBindingResult(new Object(), "obj");
        MethodArgumentNotValidException emptyException = new MethodArgumentNotValidException((MethodParameter) null, emptyResult);
        assertThat(handler.handleValidation(emptyException).getBody()).isEqualTo("Requisição inválida.");
    }
}
