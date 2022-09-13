package io.ilopezluna.application;

import io.ilopezluna.application.entities.Note;
import io.ilopezluna.application.repositories.NoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        log.info("Args:");
        log.info(Arrays.toString(args));

        log.info("Env:");
        log.info(System.getenv().toString());
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner demo(NoteRepository repository) {
        return (args) -> {
            // save a few customers
            repository.save(new Note(1L, "first note"));
            repository.save(new Note(2L, "second note"));
        };
    }
}
