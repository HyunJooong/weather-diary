/*
package choo.weather.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, WebRequest request) {
        // Swagger 관련 요청 제외
        String path = request.getDescription(false);
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            return ""; // Swagger 관련 요청은 예외 처리하지 않음
        }

        System.out.println("Error from GlobalExceptionAdvice");
        return "An internal error occurred: " + e.getMessage();
    }

}
*/
