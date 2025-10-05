package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentInfo(@PathVariable Long id) {
        logger.info("Was invoked GET method to get student by ID: {}", id);

        Student student = studentService.getStudentById(id);
        if (student == null) {
            logger.warn("Student not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning student: {}", student.getName());
        return ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity<Collection<Student>> findStudents(@RequestParam(required = false) Integer age) {
        if (age != null) {
            logger.info("Was invoked GET method to find students by age: {}", age);
            List<Student> students = studentService.getStudentsByAge(age);
            logger.debug("Found {} students with age {}", students.size(), age);
            return ResponseEntity.ok(students);
        } else {
            logger.info("Was invoked GET method to get all students");
            List<Student> allStudents = studentService.getAllStudents();
            logger.debug("Returning all {} students", allStudents.size());
            return ResponseEntity.ok(allStudents);
        }
    }

    @GetMapping("/by-age-between")
    public ResponseEntity<List<Student>> getStudentsByAgeBetween(
            @RequestParam int minAge,
            @RequestParam int maxAge) {
        logger.info("Was invoked GET method to get students by age between {} and {}", minAge, maxAge);

        try {
            List<Student> students = studentService.getStudentsByAgeBetween(minAge, maxAge);
            logger.debug("Found {} students in age range {}-{}", students.size(), minAge, maxAge);
            return ResponseEntity.ok(students);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid age range parameters: minAge={}, maxAge={}", minAge, maxAge);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalStudentCount() {
        logger.info("Was invoked GET method to get total student count");

        Integer count = studentService.getTotalCountOfStudents();
        logger.debug("Total student count: {}", count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Double> getAverageStudentAge() {
        logger.info("Was invoked GET method to get average student age");

        Double averageAge = studentService.getAverageAgeOfStudents();
        logger.debug("Average student age: {}", averageAge);
        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/last-five")
    public ResponseEntity<List<Student>> getLastFiveStudents() {
        logger.info("Was invoked GET method to get last five students");

        List<Student> students = studentService.getLastFiveStudents();
        logger.debug("Returning {} last students", students.size());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}/faculty")
    public ResponseEntity<Faculty> getStudentFaculty(@PathVariable Long id) {
        logger.info("Was invoked GET method to get faculty for student ID: {}", id);

        Faculty faculty = studentService.getStudentFaculty(id);
        if (faculty == null) {
            logger.warn("Faculty not found for student ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning faculty: {} for student ID: {}", faculty.getName(), id);
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        logger.info("Was invoked POST method to create student");
        logger.debug("Student data: name={}, age={}", student.getName(), student.getAge());

        try {
            Student createdStudent = studentService.createStudent(student);
            logger.info("Student created successfully with ID: {}", createdStudent.getId());
            return ResponseEntity.ok(createdStudent);
        } catch (Exception e) {
            logger.error("Error creating student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping
    public ResponseEntity<Student> editStudent(@RequestBody Student student) {
        logger.info("Was invoked PUT method to edit student");
        logger.debug("Editing student ID: {}, data: {}", student.getId(), student);

        Student updatedStudent = studentService.updateStudent(student.getId(), student);
        if (updatedStudent == null) {
            logger.warn("Cannot edit student. Student not found with ID: {}", student.getId());
            return ResponseEntity.notFound().build();
        }

        logger.info("Student updated successfully with ID: {}", updatedStudent.getId());
        return ResponseEntity.ok(updatedStudent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> editStudentById(@PathVariable Long id, @RequestBody Student student) {
        logger.info("Was invoked PUT method to edit student by ID: {}", id);

        Student updatedStudent = studentService.updateStudent(id, student);
        if (updatedStudent == null) {
            logger.warn("Cannot edit student. Student not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Student updated successfully with ID: {}", id);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        logger.info("Was invoked DELETE method to delete student with ID: {}", id);

        studentService.deleteStudent(id);
        logger.info("Student deletion completed for ID: {}", id);
        return ResponseEntity.ok().build();
    }

    // Методы для работы с аватарами

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam MultipartFile avatar) {
        logger.info("Was invoked POST method to upload avatar for student ID: {}", id);
        logger.debug("Avatar file: name={}, size={}, type={}",
                avatar.getOriginalFilename(),
                avatar.getSize(),
                avatar.getContentType());

        if (avatar.isEmpty()) {
            logger.warn("Empty avatar file provided for student ID: {}", id);
            return ResponseEntity.badRequest().body("Avatar file is empty");
        }

        if (avatar.getSize() > 1024 * 300) { // 300KB limit
            logger.warn("Avatar file too large for student ID: {}. Size: {} bytes", id, avatar.getSize());
            return ResponseEntity.badRequest().body("File is too big. Maximum size is 300KB");
        }

        try {
            studentService.uploadAvatar(id, avatar);
            logger.info("Avatar uploaded successfully for student ID: {}", id);
            return ResponseEntity.ok("Avatar uploaded successfully");
        } catch (IOException e) {
            logger.error("Error uploading avatar for student ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading avatar: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid student ID for avatar upload: {}", id);
            return ResponseEntity.badRequest().body("Student not found with ID: " + id);
        }
    }

    @GetMapping(value = "/{id}/avatar/preview")
    public ResponseEntity<byte[]> downloadAvatarPreview(@PathVariable Long id) {
        logger.info("Was invoked GET method to download avatar preview for student ID: {}", id);

        Avatar avatar = studentService.findAvatar(id);
        if (avatar == null) {
            logger.warn("Avatar not found for student ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getData().length);
        headers.setContentDispositionFormData("inline", "avatar-preview.jpg");

        logger.debug("Returning avatar preview for student ID: {}, size: {} bytes", id, avatar.getData().length);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(avatar.getData());
    }

    @GetMapping(value = "/{id}/avatar")
    public void downloadAvatar(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) {
        logger.info("Was invoked GET method to download full avatar for student ID: {}", id);

        Avatar avatar = studentService.findAvatar(id);
        if (avatar == null) {
            logger.warn("Avatar not found for student ID: {}", id);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        Path path = Path.of(avatar.getFilePath());
        if (!Files.exists(path)) {
            logger.error("Avatar file not found on disk: {}", avatar.getFilePath());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {

            response.setStatus(HttpStatus.OK.value());
            response.setContentType(avatar.getMediaType());
            response.setContentLengthLong(avatar.getFileSize());
            response.setHeader("Content-Disposition", "attachment; filename=\"avatar.jpg\"");

            is.transferTo(os);
            logger.debug("Avatar file streamed successfully for student ID: {}", id);

        } catch (IOException e) {
            logger.error("Error streaming avatar file for student ID {}: {}", id, e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudentsByName(@RequestParam String name) {
        logger.info("Was invoked GET method to search students by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Empty search term provided");
            return ResponseEntity.badRequest().build();
        }

        List<Student> students = studentService.findStudentsByName(name);
        logger.debug("Found {} students matching search term: {}", students.size(), name);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<Student>> getStudentsByFaculty(@PathVariable Long facultyId) {
        logger.info("Was invoked GET method to get students by faculty ID: {}", facultyId);

        List<Student> students = studentService.getStudentsByFacultyId(facultyId);
        logger.debug("Found {} students for faculty ID: {}", students.size(), facultyId);
        return ResponseEntity.ok(students);
    }

    @PatchMapping("/{id}/age")
    public ResponseEntity<Student> updateStudentAge(@PathVariable Long id, @RequestParam int age) {
        logger.info("Was invoked PATCH method to update age for student ID: {}", id);
        logger.debug("New age: {}", age);

        if (age < 16) {
            logger.warn("Invalid age provided: {}. Age must be at least 16", age);
            return ResponseEntity.badRequest().build();
        }

        Student student = studentService.getStudentById(id);
        if (student == null) {
            logger.warn("Student not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        student.setAge(age);
        Student updatedStudent = studentService.updateStudent(id, student);
        logger.info("Student age updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedStudent);
    }

    @GetMapping("/names-starting-with-a")
    public ResponseEntity<List<String>> getStudentNamesStartingWithA() {
        logger.info("Was invoked GET method to get student names starting with A");

        List<String> names = studentService.getStudentNamesStartingWithA();
        logger.debug("Found {} student names starting with A", names.size());
        return ResponseEntity.ok(names);
    }

    @GetMapping("/average-age-stream")
    public ResponseEntity<Double> getAverageAgeWithStream() {
        logger.info("Was invoked GET method to get average age using stream");

        Double averageAge = studentService.getAverageAgeWithStream();
        logger.debug("Average student age (stream): {}", averageAge);
        return ResponseEntity.ok(averageAge);
    }

    @GetMapping("/math/sum-optimized")
    public ResponseEntity<Long> calculateSumOptimized() {
        logger.info("Was invoked GET method to calculate optimized sum");
        long startTime = System.currentTimeMillis();
        long n = 1_000_000L;
        long sum = n * (1 + n) / 2;
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("Sum calculation completed in {} ms. Result: {}", duration, sum);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/math/sum-original")
    public ResponseEntity<Integer> calculateSumOriginal() {
        logger.info("Was invoked GET method to calculate original sum");
        long startTime = System.currentTimeMillis();
        int sum = java.util.stream.Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, (a, b) -> a + b);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("Original sum calculation completed in {} ms. Result: {}", duration, sum);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/print-parallel")
    public ResponseEntity<String> printStudentsParallel() {
        logger.info("Was invoked GET method to print students in parallel");

        studentService.printStudentsParallel();
        return ResponseEntity.ok("Students printed in parallel mode");
    }

    @GetMapping("/print-synchronized")
    public ResponseEntity<String> printStudentsSynchronized() {
        logger.info("Was invoked GET method to print students in synchronized mode");

        studentService.printStudentsSynchronized();
        return ResponseEntity.ok("Students printed in synchronized mode");
    }
}