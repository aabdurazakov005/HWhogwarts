package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.hogwarts.school.model.Student;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTestWithTestRestTemplate {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    void testGetStudentInfo() {
        Student student = new Student(null, "Harry Potter", 17);
        Student createdStudent = restTemplate.postForObject(getBaseUrl(), student, Student.class);


        ResponseEntity<Student> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + createdStudent.getId(), Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Harry Potter", response.getBody().getName());
    }

    @Test
    void testFindStudentsByAge() {
        restTemplate.postForObject(getBaseUrl(), new Student(null, "Ron Weasley", 16), Student.class);
        restTemplate.postForObject(getBaseUrl(), new Student(null, "Hermione Granger", 17), Student.class);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                getBaseUrl() + "?age=17", Student[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Hermione Granger", response.getBody()[0].getName());
    }

    @Test
    void testCreateStudent() {
        Student student = new Student(null, "Draco Malfoy", 18);

        ResponseEntity<Student> response = restTemplate.postForEntity(
                getBaseUrl(), student, Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Draco Malfoy", response.getBody().getName());
    }

    @Test
    void testEditStudent() {
        Student student = new Student(null, "Neville Longbottom", 16);
        Student createdStudent = restTemplate.postForObject(getBaseUrl(), student, Student.class);

        createdStudent.setAge(17);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> entity = new HttpEntity<>(createdStudent, headers);

        ResponseEntity<Student> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.PUT, entity, Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(17, response.getBody().getAge());
    }

    @Test
    void testDeleteStudent() {
        Student student = new Student(null, "Luna Lovegood", 15);
        Student createdStudent = restTemplate.postForObject(getBaseUrl(), student, Student.class);

        restTemplate.delete(getBaseUrl() + "/" + createdStudent.getId());

        ResponseEntity<Student> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + createdStudent.getId(), Student.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}