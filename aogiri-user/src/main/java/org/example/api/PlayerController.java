package org.example.api;

import jakarta.validation.Valid;
import org.example.application.PlayerCreateDTO;
import org.example.application.PlayerDTO;
import org.example.application.PlayerService;
import org.springframework.web.bind.annotation.*;
import org.example.application.TokenService;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/players", "/api/v1/users"})
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service, TokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<PlayerDTO> getAll() {
        return service.getAllPlayers();
    }

    @GetMapping("/{id}")
    public PlayerDTO getById(@PathVariable Long id) {
        return service.getPlayerById(id);
    }

    @PostMapping
    public PlayerDTO create(@Valid @RequestBody PlayerCreateDTO dto) {
        return service.createPlayer(dto);
    }

    @PutMapping("/{id}")
    public PlayerDTO update(@PathVariable Long id, @Valid @RequestBody PlayerCreateDTO dto) {
        return service.updatePlayer(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePlayer(id);
    }

    private final TokenService tokenService; // додай у конструктор

    @GetMapping("/{id}/login")
    public String login(@PathVariable Long id) {
        PlayerDTO player = service.getPlayerById(id);
        return tokenService.generateToken(id, player.nickname());
    }

}