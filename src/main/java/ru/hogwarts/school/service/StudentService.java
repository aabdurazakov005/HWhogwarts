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
import java.util.stream.Collectors;

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

    public List<String> getStudentNamesStartingWithA() {
        logger.info("Was invoked method for get student names starting with A");

        List<String> names = studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name.toUpperCase().startsWith("А")) // Русская А
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());

        logger.debug("Found {} student names starting with A", names.size());
        return names;
    }

    public Double getAverageAgeWithStream() {
        logger.info("Was invoked method for get average age using stream");

        List<Student> students = studentRepository.findAll();

        if (students.isEmpty()) {
            logger.debug("No students found for average age calculation");
            return 0.0;
        }

        Double averageAge = students.stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);

        logger.debug("Average age calculated: {}", averageAge);
        return averageAge;
    }

    public void printStudentsParallel() {
        logger.info("Was invoked method for parallel printing of students");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students for parallel printing. Need at least 6, found: {}", students.size());
            return;
        }

        logger.info("Starting parallel printing of {} students", students.size());

        System.out.println("Main Thread: " + students.get(0).getName());
        System.out.println("Main Thread: " + students.get(1).getName());

        Thread thread1 = new Thread(() -> {
            System.out.println("Parallel Thread 1: " + students.get(2).getName());
            System.out.println("Parallel Thread 1: " + students.get(3).getName());
        });

        Thread thread2 = new Thread(() -> {
            System.out.println("Parallel Thread 2: " + students.get(4).getName());
            System.out.println("Parallel Thread 2: " + students.get(5).getName());
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted during parallel printing", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Parallel printing completed");
    }

    public void printStudentsSynchronized() {
        logger.info("Was invoked method for synchronized printing of students");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students for synchronized printing. Need at least 6, found: {}", students.size());
            return;
        }

        logger.info("Starting synchronized printing of {} students", students.size());

        printStudentNameSynchronized(students.get(0).getName(), "Main Thread");
        printStudentNameSynchronized(students.get(1).getName(), "Main Thread");

        Thread thread1 = new Thread(() -> {
            printStudentNameSynchronized(students.get(2).getName(), "Parallel Thread 1");
            printStudentNameSynchronized(students.get(3).getName(), "Parallel Thread 1");
        });

        Thread thread2 = new Thread(() -> {
            printStudentNameSynchronized(students.get(4).getName(), "Parallel Thread 2");
            printStudentNameSynchronized(students.get(5).getName(), "Parallel Thread 2");
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted during synchronized printing", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Synchronized printing completed");
    }

    private synchronized void printStudentNameSynchronized(String studentName, String threadName) {
        System.out.println(threadName + " (synchronized): " + studentName);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}