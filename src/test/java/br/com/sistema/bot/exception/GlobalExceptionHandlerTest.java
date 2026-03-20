package br.com.sistema.bot.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar unprocessable entity para erro de validacao")
    void deveRetornarUnprocessableEntityParaErroDeValidacao() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "cpf", "inválido"));

        Method method = DummyController.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        HttpServletRequest request = request("/webhook/whatsapp");

        Map<String, Object> body = handler.handleValidation(ex, request).getBody();

        assertEquals(422, body.get("status"));
        assertEquals("Erro de validação", body.get("error"));
        assertEquals("/webhook/whatsapp", body.get("path"));
    }

    @Test
    @DisplayName("Deve retornar not found para entity not found")
    void deveRetornarNotFoundParaEntityNotFound() {
        HttpServletRequest request = request("/x");

        Map<String, Object> body = handler.handleEntityNotFound(new EntityNotFoundException("nao existe"), request).getBody();

        assertEquals(404, body.get("status"));
        assertEquals("Recurso não encontrado", body.get("error"));
    }

    @Test
    @DisplayName("Deve retornar internal server error para excecao generica")
    void deveRetornarInternalServerErrorParaExcecaoGenerica() {
        HttpServletRequest request = request("/y");

        Map<String, Object> body = handler.handleGeneric(new RuntimeException("erro"), request).getBody();

        assertEquals(500, body.get("status"));
        assertEquals("Erro interno do servidor", body.get("error"));
        assertEquals("erro", body.get("message"));
    }

    private HttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    static class DummyController {
        @SuppressWarnings("unused")
        public void dummy(String value) {
        }
    }
}
