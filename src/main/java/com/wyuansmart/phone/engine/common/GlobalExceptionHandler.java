package com.wyuansmart.phone.engine.common;

import com.wyuansmart.phone.common.base.dto.JsonObject;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        return new ResponseEntity(processFieldErrors(fieldErrors), HttpStatus.BAD_REQUEST);
    }

    private JsonObject processFieldErrors(List<FieldError> fieldErrors) {
        JsonObject jsonObject = new JsonObject(FieldErrorResponse.fromFieldErrors(fieldErrors),
                HttpStatus.BAD_REQUEST.value(), "validation error");

        return jsonObject;
    }

    static class FieldErrorResponse {
        private String message;
        private String field;
        private String object;

        public FieldErrorResponse(String message, String field, String object) {
            this.message = message;
            this.field = field;
            this.object = object;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public static List<FieldErrorResponse> fromFieldErrors(Collection<FieldError> fieldErrors) {
            List responseList = new ArrayList();
            for (Iterator<FieldError> it = fieldErrors.iterator(); it.hasNext();) {
                FieldError fieldError = it.next();
                responseList.add(new FieldErrorResponse(fieldError.getDefaultMessage(),
                        fieldError.getField(), fieldError.getObjectName()));
            }
            return responseList;
        }
    }
}