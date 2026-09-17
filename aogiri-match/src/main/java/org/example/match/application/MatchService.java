package org.example.match.application;

import org.example.match.infrastructure.MatchEntity;
import org.example.match.infrastructure.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {
    private final MatchRepository repository;
    public MatchService(MatchRepository repository) { this.repository = repository; }

    private MatchDTO convertToDto(MatchEntity entity) {
        return new MatchDTO(entity.getId(), entity.getTeams(), entity.getMatchDate());
    }

    public List<MatchDTO> getAll() {
        return repository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public MatchDTO getById(Long id) {
        return repository.findById(id).map(this::convertToDto).orElse(null);
    }

    public MatchDTO create(MatchCreateDTO dto) {
        MatchEntity saved = repository.save(new MatchEntity(dto.teams(), dto.matchDate()));
        return convertToDto(saved);
    }

    public MatchDTO update(Long id, MatchCreateDTO dto) {
        return repository.findById(id).map(entity -> {
            entity.setTeams(dto.teams());
            entity.setMatchDate(dto.matchDate());
            return convertToDto(repository.save(entity));
        }).orElse(null);
    }

    public void delete(Long id) { repository.deleteById(id); }
}