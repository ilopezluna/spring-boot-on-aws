package io.ilopezluna.application.controllers;

import io.ilopezluna.application.entities.Note;
import io.ilopezluna.application.repositories.NoteRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class NoteController {

    private final NoteRepository repository;

    NoteController(NoteRepository repository) {
        this.repository = repository;
    }


    @GetMapping("/notes")
    List<Note> all() {
        return repository.findAll();
    }

    @PostMapping("/notes")
    Note newNote(@RequestBody Note note) {
        return repository.save(note);
    }
}
