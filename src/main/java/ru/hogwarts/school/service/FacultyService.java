package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        logger.debug("Creating faculty: {}", faculty.getName());

        try {
            Faculty savedFaculty = facultyRepository.save(faculty);
            logger.info("Faculty created successfully with ID: {}", savedFaculty.getId());
            return savedFaculty;
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage());
            throw e;
        }
    }

    public Faculty getFacultyById(Long id) {
        logger.info("Was invoked method for get faculty by ID: {}", id);

        Optional<Faculty> faculty = facultyRepository.findById(id);
        if (faculty.isEmpty()) {
            logger.warn("Faculty not found with ID: {}", id);
            return null;
        }

        logger.debug("Found faculty: {}", faculty.get().getName());
        return faculty.get();
    }

    public Faculty updateFaculty(Long id, Faculty faculty) {
        logger.info("Was invoked method for update faculty with ID: {}", id);

        Faculty existingFaculty = getFacultyById(id);
        if (existingFaculty == null) {
            logger.error("Cannot update faculty. Faculty not found with ID: {}", id);
            return null;
        }

        existingFaculty.setName(faculty.getName());
        existingFaculty.setColor(faculty.getColor());

        Faculty updatedFaculty = facultyRepository.save(existingFaculty);
        logger.info("Faculty updated successfully with ID: {}", id);
        return updatedFaculty;
    }

    public void deleteFaculty(Long id) {
        logger.info("Was invoked method for delete faculty with ID: {}", id);

        if (!facultyRepository.existsById(id)) {
            logger.warn("Cannot delete faculty. Faculty not found with ID: {}", id);
            return;
        }

        facultyRepository.deleteById(id);
        logger.info("Faculty deleted successfully with ID: {}", id);
    }

    public List<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");

        List<Faculty> faculties = facultyRepository.findAll();
        logger.debug("Found {} faculties", faculties.size());
        return faculties;
    }

    public List<Faculty> getFacultiesByColor(String color) {
        logger.info("Was invoked method for get faculties by color: {}", color);

        List<Faculty> faculties = facultyRepository.findByColor(color);
        logger.debug("Found {} faculties with color {}", faculties.size(), color);
        return faculties;
    }

    public List<Faculty> getFacultiesByNameOrColor(String searchTerm) {
        logger.info("Was invoked method for get faculties by name or color: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.warn("Search term is empty or null");
            return List.of();
        }

        List<Faculty> faculties = facultyRepository.findByNameIgnoreCaseOrColorIgnoreCase(searchTerm, searchTerm);
        logger.debug("Found {} faculties matching search term: {}", faculties.size(), searchTerm);
        return faculties;
    }

    public List<Student> getFacultyStudents(Long facultyId) {
        logger.info("Was invoked method for get faculty students with faculty ID: {}", facultyId);

        Faculty faculty = getFacultyById(facultyId);
        if (faculty == null) {
            logger.warn("Cannot get students. Faculty not found with ID: {}", facultyId);
            return List.of();
        }

        List<Student> students = faculty.getStudents();
        logger.debug("Found {} students in faculty {}", students.size(), faculty.getName());
        return students;
    }

    public Faculty getFacultyByName(String name) {
        logger.info("Was invoked method for get faculty by name: {}", name);

        Optional<Faculty> faculty = facultyRepository.findByNameIgnoreCase(name);
        if (faculty.isEmpty()) {
            logger.debug("Faculty not found with name: {}", name);
            return null;
        }

        logger.debug("Found faculty: {} with name: {}", faculty.get().getId(), name);
        return faculty.get();
    }
}