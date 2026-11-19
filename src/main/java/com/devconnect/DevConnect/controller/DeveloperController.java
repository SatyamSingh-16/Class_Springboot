package com.devconnect.DevConnect.controller;

import com.devconnect.DevConnect.model.Developer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/developers")
public class DeveloperController {

    // ─── In-Memory Storage ────────────────────────────────────────
    // ConcurrentHashMap is thread-safe — safe for concurrent requests
    // AtomicLong is thread-safe for ID generation
    // WARNING: All data is lost when the server restarts!
    private final Map<Long, Developer> developerStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @GetMapping
    public List<Developer> getAllDevelopers() {
        return new ArrayList<>(developerStore.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Developer> getDeveloperById(@PathVariable Long id) {
        Developer developer = developerStore.get(id);
        return developer == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(developer);
    }

    @GetMapping("/{skill1}/{skill2}")
    public List<Developer> getDevelopersBySkills(@PathVariable String skill1, @PathVariable String skill2) {
        return developerStore.values().stream()
                .filter(developer -> developer.getSkills() != null)
                .filter(developer -> developer.getSkills().stream()
                        .anyMatch(skill -> skill.equalsIgnoreCase(skill1)))
                .filter(developer -> developer.getSkills().stream()
                        .anyMatch(skill -> skill.equalsIgnoreCase(skill2)))
                .collect(Collectors.toList());
    }

    // GET /api/v1/developes/search?skill=Java&city=Bangalore&count=15
    @GetMapping("/search")
    public List<Developer> getDevelopersBySkillAndLocation(
             @RequestParam String skill,
             @RequestParam(required = false) String city,
             @RequestParam(defaultValue = "10") int count) {
        return developerStore.values().stream()
                .filter(developer -> developer.getSkills() != null)
                .filter(developer -> developer.getSkills().stream()
                        .anyMatch(developerSkill -> developerSkill.equalsIgnoreCase(skill)))
                .filter(developer -> city == null
                        || (developer.getCity() != null && developer.getCity().equalsIgnoreCase(city)))
                .limit(Math.max(0, count))
                .collect(Collectors.toList());
    }


    // Body: {"name":"Riya","email":"riya@test.com","skills":["Java"]}
    @PostMapping
    public ResponseEntity<Developer> createDeveloper(@RequestBody Developer developer) {
        boolean duplicate = developerStore.values().stream()
                .anyMatch(existing -> sameValue(existing.getPhoneNumber(), developer.getPhoneNumber())
                        || sameValue(existing.getEmail(), developer.getEmail()));
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(developer);
        }

        long id = idCounter.getAndIncrement();
        developer.setId(id);
        developerStore.put(id, developer);
        return ResponseEntity.status(HttpStatus.CREATED).body(developer);
    }

    @PutMapping("api/v1/developer/{id}")
    public ResponseEntity<Developer> updateDeveloper(
            @PathVariable Long id, @RequestBody Developer developer) {
        if (!developerStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        developer.setId(id);
        developerStore.put(id, developer);
        return ResponseEntity.ok(developer);
    }

    @PatchMapping("api/v1/developer/{id}")
    public ResponseEntity<Developer> patchDeveloper(
            @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Developer developer = developerStore.get(id);
        if (developer == null) {
            return ResponseEntity.notFound().build();
        }

        updates.forEach((field, value) -> applyUpdate(developer, field, value));
        return ResponseEntity.ok(developer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        return developerStore.remove(id) == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.noContent().build();
    }

    private void applyUpdate(Developer developer, String field, Object value) {
        switch (field) {
            case "name" -> developer.setName(asString(field, value));
            case "phoneNumber" -> developer.setPhoneNumber(asString(field, value));
            case "email" -> developer.setEmail(asString(field, value));
            case "bio" -> developer.setBio(asString(field, value));
            case "city" -> developer.setCity(asString(field, value));
            case "linkedInURL" -> developer.setLinkedInURL(asString(field, value));
            case "githubInURL" -> developer.setGithubInURL(asString(field, value));
            case "skills" -> developer.setSkills(asStringList(field, value));
            case "yearsOfExperience" -> developer.setYearsOfExperience(asDouble(field, value));
            case "id", "createdAt" ->
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Field cannot be updated: " + field);
            default ->
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Unknown field: " + field);
        }
    }

    private static boolean sameValue(String first, String second) {
        return first != null && first.equals(second);
    }

    private static String asString(String field, Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Field must be a string: " + field);
    }

    private static List<String> asStringList(String field, Object value) {
        if (!(value instanceof List<?> values)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Field must be a list of strings: " + field);
        }
        List<String> result = new ArrayList<>();
        for (Object item : values) {
            if (!(item instanceof String stringValue)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Field must be a list of strings: " + field);
            }
            result.add(stringValue);
        }
        return result;
    }

    private static Double asDouble(String field, Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Field must be numeric: " + field);
    }
}
