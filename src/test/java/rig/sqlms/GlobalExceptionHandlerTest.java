package rig.sqlms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import rig.sqlms.exception.UnknownDataSourceNameException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ErrorResponseBody is a private record on GlobalExceptionHandler, so tests inspect the
    // response the same way an HTTP client would: as serialized JSON.
    private Map<String, Object> bodyAsMap(ResponseEntity<?> response) throws Exception {
        return objectMapper.readValue(objectMapper.writeValueAsString(response.getBody()), Map.class);
    }

    @Test
    void handleInternalException_shouldReturnBadRequestWithErrorAndMessage() throws Exception {
        ResponseEntity<?> response = handler.handleInternalException(
                new UnknownDataSourceNameException("crm"), mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of(
                "error", "UnknownDataSourceNameException",
                "message", "Specified dataSourceName name: 'crm' is unknown to the service"
        ), bodyAsMap(response));
    }

    @Test
    void handleException_shouldReturnBadRequestWithTheExceptionMessage() throws Exception {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/some-endpoint");

        ResponseEntity<?> response = handler.handleException(new IllegalStateException("boom"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of(
                "error", "IllegalStateException",
                "message", "boom"
        ), bodyAsMap(response));
    }

    @Test
    void handleException_shouldFallBackToAGenericMessageWhenTheExceptionHasNone() throws Exception {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/some-endpoint");

        ResponseEntity<?> response = handler.handleException(new NullPointerException(), request);

        assertEquals(Map.of(
                "error", "NullPointerException",
                "message", "Internal error"
        ), bodyAsMap(response));
    }
}
