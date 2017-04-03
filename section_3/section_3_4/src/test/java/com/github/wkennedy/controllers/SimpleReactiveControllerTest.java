package com.github.wkennedy.controllers;

import com.github.wkennedy.dto.Person;
import com.github.wkennedy.services.SimpleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;
import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
//@WebFluxTest
public class SimpleReactiveControllerTest {


    private WebTestClient webTestClientReact;


    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SimpleService simpleService;

    @Before
    public void before() {
//        this.webTestClientReact = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:8080").build();
        this.webTestClientReact = WebTestClient.bindToController(new SimpleReactiveController(simpleService)).build();
    }

    @Test
    public void getReactPersons() throws Exception {
        Flux<Person> personFlux = webTestClientReact.get().uri("/react/persons").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is2xxSuccessful().expectBody(Person.class).returnResult().getResponseBody().cast(Person.class);
        personFlux.delaySubscription(Duration.ofSeconds(0)).toStream()
                .forEach(s -> System.out.println(1 + ": " + s));
    }

    @Test
    public void testStreaming() throws URISyntaxException {
        FluxExchangeResult<Person> personResult = webTestClientReact.get().uri("/react/persons").accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange().expectStatus().is2xxSuccessful()
                .expectHeader().contentType(MediaType.parseMediaType("application/stream+json;charset=UTF-8"))
                .expectBody(Person.class).returnResult();

        personResult.getResponseBody().toStream().forEach(person -> System.out.println("WebTestClient: " + person.toString()));

        System.out.println("Ending Test");
    }

//    @Test
//    public void testWebSocketClient() {
//        WebSocketClient client = new StandardWebSocketClient();
//        client.execute("ws://localhost:8080/react/persons").log();//, session -> {... }).blockMillis(5000);
//    }


    public class ReactWorker extends Thread {
        private String name;
        private Long delay;

        ReactWorker(String name, Long delay) {
            this.name = name;
            this.delay = delay;
        }
        public void run() {
            Flux<Person> personFlux = webTestClientReact.get().uri("/react/persons/delay/" + delay).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().is2xxSuccessful().expectBody(Person.class).returnResult().getResponseBody().cast(Person.class);
            personFlux.delaySubscription(Duration.ofSeconds(delay)).toStream()
                    .forEach(s -> System.out.println(name + ": " + s));
        }

    }


}