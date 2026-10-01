package com.creasevision.api.controller;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.creasevision.api.service.NotFoundException;
@RestControllerAdvice public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST) ApiError invalid(Exception e){return new ApiError(Instant.now(),400,"bad_request",e.getMessage());}
    @ExceptionHandler(NotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND) ApiError missing(NotFoundException e){return new ApiError(Instant.now(),404,"not_found",e.getMessage());}
    public record ApiError(Instant timestamp,int status,String code,String message){}
}
