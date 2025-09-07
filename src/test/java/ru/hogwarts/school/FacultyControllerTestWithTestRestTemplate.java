package ru.hogwarts.school;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.hogwarts.school.model.Faculty;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTestWithTestRestTemplate {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/faculty";
    }

    @Test
    void testGetFacultyInfo() {
        Faculty faculty = new Faculty(null, "Gryffindor", "red");
        Faculty createdFaculty = restTemplate.postForObject(getBaseUrl(), faculty, Faculty.class);

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + createdFaculty.getId(), Faculty.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Gryffindor", response.getBody().getName());
    }

    @Test
    void testCreateFaculty() {
        Faculty faculty = new Faculty(null, "Slytherin", "green");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                getBaseUrl(), faculty, Faculty.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Slytherin", response.getBody().getName());
    }

    @Test
    void testEditFaculty() {
        Faculty faculty = new Faculty(null, "Hufflepuff", "yellow");
        Faculty createdFaculty = restTemplate.postForObject(getBaseUrl(), faculty, Faculty.class);

        createdFaculty.setColor("black and yellow");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> entity = new HttpEntity<>(createdFaculty, headers);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.PUT, entity, Faculty.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("black and yellow", response.getBody().getColor());
    }

    @Test
    void testDeleteFaculty() {
        Faculty faculty = new Faculty(null, "Ravenclaw", "blue");
        Faculty createdFaculty = restTemplate.postForObject(getBaseUrl(), faculty, Faculty.class);

        restTemplate.delete(getBaseUrl() + "/" + createdFaculty.getId());

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + createdFaculty.getId(), Faculty.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetFacultiesByColor() {
        restTemplate.postForObject(getBaseUrl(), new Faculty(null, "Gryffindor", "red"), Faculty.class);
        restTemplate.postForObject(getBaseUrl(), new Faculty(null, "Ravenclaw", "blue"), Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/by-color?color=red", Faculty[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Gryffindor", response.getBody()[0].getName());
    }

    @Test
    void testSearchFaculties() {
        restTemplate.postForObject(getBaseUrl(), new Faculty(null, "Gryffindor", "red"), Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/search?searchTerm=gryff", Faculty[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Gryffindor", response.getBody()[0].getName());
    }
}