package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        logger.debug("Creating student: {}", student.getName());

        try {
            Student savedStudent = studentRepository.save(student);
            logger.info("Student created successfully with ID: {}", savedStudent.getId());
            return savedStudent;
        } catch (Exception e) {
            logger.error("Error creating student: {}", e.getMessage());
            throw e;
        }
    }

    public Student getStudentById(Long id) {
        logger.info("Was invoked method for get student by ID: {}", id);
        logger.debug("Searching for student with ID: {}", id);

        Optional<Student> student = studentRepository.findById(id);
        if (student.isEmpty()) {
            logger.warn("Student not found with ID: {}", id);
            return null;
        }

        logger.debug("Found student: {}", student.get().getName());
        return student.get();
    }

    public Student updateStudent(Long id, Student student) {
        logger.info("Was invoked method for update student with ID: {}", id);
        logger.debug("Updating student ID {} with data: {}", id, student);

        Student existingStudent = getStudentById(id);
        if (existingStudent == null) {
            logger.error("Cannot update student. Student not found with ID: {}", id);
            return null;
        }

        existingStudent.setName(student.getName());
        existingStudent.setAge(student.getAge());
        existingStudent.setFaculty(student.getFaculty());

        Student updatedStudent = studentRepository.save(existingStudent);
        logger.info("Student updated successfully with ID: {}", id);
        return updatedStudent;
    }

    public void deleteStudent(Long id) {
        logger.info("Was invoked method for delete student with ID: {}", id);

        if (!studentRepository.existsById(id)) {
            logger.warn("Cannot delete student. Student not found with ID: {}", id);
            return;
        }

        studentRepository.deleteById(id);
        logger.info("Student deleted successfully with ID: {}", id);
    }

    public List<Student> getAllStudents() {
        logger.info("Was invoked method for get all students");

        List<Student> students = studentRepository.findAll();
        logger.debug("Found {} students", students.size());
        return students;
    }

    public List<Student> getStudentsByAge(int age) {
        logger.info("Was invoked method for get students by age: {}", age);
        logger.debug("Searching students with age: {}", age);

        List<Student> students = studentRepository.findByAge(age);
        logger.debug("Found {} students with age {}", students.size(), age);
        return students;
    }

    public List<Student> getStudentsByAgeBetween(int minAge, int maxAge) {
        logger.info("Was invoked method for get students by age between {} and {}", minAge, maxAge);

        if (minAge > maxAge) {
            logger.warn("Invalid age range: minAge {} is greater than maxAge {}", minAge, maxAge);
            throw new IllegalArgumentException("minAge cannot be greater than maxAge");
        }

        List<Student> students = studentRepository.findByAgeBetween(minAge, maxAge);
        logger.debug("Found {} students in age range {}-{}", students.size(), minAge, maxAge);
        return students;
    }

    public Integer getTotalCountOfStudents() {
        logger.info("Was invoked method for get total count of students");

        Integer count = studentRepository.getTotalCountOfStudents();
        logger.debug("Total student count: {}", count);
        return count;
    }

    public Double getAverageAgeOfStudents() {
        logger.info("Was invoked method for get average age of students");

        Double averageAge = studentRepository.getAverageAgeOfStudents();
        logger.debug("Average student age: {}", averageAge);
        return averageAge;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");

        List<Student> students = studentRepository.getLastFiveStudents();
        logger.debug("Retrieved {} last students", students.size());
        return students;
    }

    public List<Student> findStudentsByName(String name) {
        logger.info("Was invoked method for find students by name: {}", name);
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Student> getStudentsByFacultyId(Long facultyId) {
        logger.info("Was invoked method for get students by faculty ID: {}", facultyId);
        return studentRepository.findByFacultyId(facultyId);
    }

    public Faculty getStudentFaculty(Long studentId) {
        logger.info("Was invoked method for get student faculty for student ID: {}", studentId);

        Student student = getStudentById(studentId);
        if (student == null) {
            logger.warn("Student not found with ID: {}", studentId);
            return null;
        }

        return student.getFaculty();
    }

    public void uploadAvatar(Long studentId, MultipartFile avatar) throws IOException {
        logger.info("Uploading avatar for student ID: {}", studentId);
    }

    public Avatar findAvatar(Long studentId) {
        logger.info("Finding avatar for student ID: {}", studentId);
        return null;
    }
}