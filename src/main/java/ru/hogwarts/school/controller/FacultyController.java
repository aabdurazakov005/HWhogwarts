package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private static final Logger logger = LoggerFactory.getLogger(FacultyController.class);

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<List<Faculty>> getAllFaculties() {
        logger.info("Was invoked GET method to get all faculties");

        List<Faculty> faculties = facultyService.getAllFaculties();
        logger.debug("Returning {} faculties", faculties.size());
        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Faculty> getFacultyInfo(@PathVariable Long id) {
        logger.info("Was invoked GET method to get faculty by ID: {}", id);

        Faculty faculty = facultyService.getFacultyById(id);
        if (faculty == null) {
            logger.warn("Faculty not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning faculty: {} (ID: {})", faculty.getName(), faculty.getId());
        return ResponseEntity.ok(faculty);
    }

    @GetMapping("/by-color")
    public ResponseEntity<List<Faculty>> getFacultiesByColor(@RequestParam String color) {
        logger.info("Was invoked GET method to get faculties by color: {}", color);

        if (color == null || color.trim().isEmpty()) {
            logger.warn("Empty color parameter provided");
            return ResponseEntity.badRequest().build();
        }

        List<Faculty> faculties = facultyService.getFacultiesByColor(color);
        logger.debug("Found {} faculties with color: {}", faculties.size(), color);

        if (faculties.isEmpty()) {
            logger.info("No faculties found with color: {}", color);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Faculty>> searchFaculties(@RequestParam String searchTerm) {
        logger.info("Was invoked GET method to search faculties by term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.warn("Empty search term provided");
            return ResponseEntity.badRequest().build();
        }

        List<Faculty> faculties = facultyService.getFacultiesByNameOrColor(searchTerm);
        logger.debug("Found {} faculties matching search term: {}", faculties.size(), searchTerm);

        return ResponseEntity.ok(faculties);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<Student>> getFacultyStudents(@PathVariable Long id) {
        logger.info("Was invoked GET method to get students for faculty ID: {}", id);

        List<Student> students = facultyService.getFacultyStudents(id);
        logger.debug("Found {} students for faculty ID: {}", students.size(), id);

        if (students.isEmpty()) {
            logger.info("No students found for faculty ID: {}", id);
        }

        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-name")
    public ResponseEntity<Faculty> getFacultyByName(@RequestParam String name) {
        logger.info("Was invoked GET method to get faculty by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Empty name parameter provided");
            return ResponseEntity.badRequest().build();
        }

        Faculty faculty = facultyService.getFacultyByName(name);
        if (faculty == null) {
            logger.info("Faculty not found with name: {}", name);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning faculty: {} (ID: {})", faculty.getName(), faculty.getId());
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public ResponseEntity<Faculty> createFaculty(@Valid @RequestBody Faculty faculty) {
        logger.info("Was invoked POST method to create faculty");
        logger.debug("Faculty data: name={}, color={}", faculty.getName(), faculty.getColor());

        try {
            Faculty createdFaculty = facultyService.createFaculty(faculty);
            logger.info("Faculty created successfully with ID: {}", createdFaculty.getId());
            return ResponseEntity.ok(createdFaculty);
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping
    public ResponseEntity<Faculty> editFaculty(@Valid @RequestBody Faculty faculty) {
        logger.info("Was invoked PUT method to edit faculty");
        logger.debug("Editing faculty ID: {}, data: name={}, color={}",
                faculty.getId(), faculty.getName(), faculty.getColor());

        Faculty updatedFaculty = facultyService.updateFaculty(faculty.getId(), faculty);
        if (updatedFaculty == null) {
            logger.warn("Cannot edit faculty. Faculty not found with ID: {}", faculty.getId());
            return ResponseEntity.notFound().build();
        }

        logger.info("Faculty updated successfully with ID: {}", updatedFaculty.getId());
        return ResponseEntity.ok(updatedFaculty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faculty> editFacultyById(@PathVariable Long id, @Valid @RequestBody Faculty faculty) {
        logger.info("Was invoked PUT method to edit faculty by ID: {}", id);
        logger.debug("New faculty data: name={}, color={}", faculty.getName(), faculty.getColor());

        Faculty updatedFaculty = facultyService.updateFaculty(id, faculty);
        if (updatedFaculty == null) {
            logger.warn("Cannot edit faculty. Faculty not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Faculty updated successfully with ID: {}", id);
        return ResponseEntity.ok(updatedFaculty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        logger.info("Was invoked DELETE method to delete faculty with ID: {}", id);

        Faculty faculty = facultyService.getFacultyById(id);
        if (faculty == null) {
            logger.warn("Cannot delete faculty. Faculty not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        facultyService.deleteFaculty(id);
        logger.info("Faculty deleted successfully with ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/longest-name")
    public ResponseEntity<String> getFacultyWithLongestName() {
        logger.info("Was invoked GET method to get faculty with longest name");

        String longestName = facultyService.getFacultyWithLongestName();
        if (longestName == null || longestName.isEmpty()) {
            logger.info("No faculties found");
            return ResponseEntity.notFound().build();
        }

        logger.debug("Longest faculty name: {}", longestName);
        return ResponseEntity.ok(longestName);
    }
}