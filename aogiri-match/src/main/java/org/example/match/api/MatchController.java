package org.example.match.api;

import jakarta.validation.Valid;
import org.example.match.application.MatchCreateDTO;
import org.example.match.application.MatchDTO;
import org.example.match.application.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService service;
    public MatchController(MatchService service) { this.service = service; }

    @GetMapping
    public List<MatchDTO> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public MatchDTO getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public MatchDTO create(@Valid @RequestBody MatchCreateDTO dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public MatchDTO update(@PathVariable Long id, @Valid @RequestBody MatchCreateDTO dto) { return service.update(id, dto); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}