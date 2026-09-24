package br.com.fiap.spacemining.services;

import br.com.fiap.spacemining.web.CommandRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CommandServiceTests {
    @Test
    void forwardsCommandAsJsonOverHttp() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://validator/commands"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"command\":\"OPEN\"}"))
                .andRespond(withStatus(HttpStatus.ACCEPTED));
        new CommandService(rest, "http://validator/commands").send(new CommandRequest("OPEN"));
        server.verify();
    }

    @Test
    void propagatesValidationError() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://validator/commands")).andRespond(withBadRequest());
        CommandService service = new CommandService(rest, "http://validator/commands");
        assertThrows(HttpClientErrorException.class, () -> service.send(new CommandRequest("INVALID")));
        server.verify();
    }
}
