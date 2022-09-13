package io.ilopezluna.application;

import io.ilopezluna.application.entities.Note;
import io.ilopezluna.application.repositories.NoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
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
