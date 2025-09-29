package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerTestWithWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacultyService facultyService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Faculty faculty1 = new Faculty(1L, "Gryffindor", "red");
    private final Faculty faculty2 = new Faculty(2L, "Slytherin", "green");
    private final Faculty faculty3 = new Faculty(3L, "Ravenclaw", "blue");

    @Test
    void testGetAllFaculties() throws Exception {
        List<Faculty> faculties = Arrays.asList(faculty1, faculty2, faculty3);
        when(facultyService.getAllFaculties()).thenReturn(faculties);

        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"))
                .andExpect(jsonPath("$[0].color").value("red"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Slytherin"))
                .andExpect(jsonPath("$[1].color").value("green"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Ravenclaw"))
                .andExpect(jsonPath("$[2].color").value("blue"));
    }

    @Test
    void testGetFacultyInfo() throws Exception {
        when(facultyService.getFacultyById(1L)).thenReturn(faculty1);

        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    void testGetFacultyInfo_NotFound() throws Exception {
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFacultiesByColor() throws Exception {
        List<Faculty> redFaculties = Collections.singletonList(faculty1);
        when(facultyService.getFacultiesByColor("red")).thenReturn(redFaculties);

        mockMvc.perform(get("/faculty/by-color")
                        .param("color", "red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"))
                .andExpect(jsonPath("$[0].color").value("red"));
    }

    @Test
    void testGetFacultiesByColor_Empty() throws Exception {
        when(facultyService.getFacultiesByColor("yellow")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/by-color")
                        .param("color", "yellow"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchFaculties() throws Exception {
        List<Faculty> foundFaculties = Arrays.asList(faculty1, faculty3);
        when(facultyService.getFacultiesByNameOrColor("gryff")).thenReturn(foundFaculties);

        mockMvc.perform(get("/faculty/search")
                        .param("searchTerm", "gryff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"))
                .andExpect(jsonPath("$[1].name").value("Ravenclaw"));
    }

    @Test
    void testGetFacultyStudents() throws Exception {
        Student student1 = new Student(1L, "Harry Potter", 17, faculty1);
        Student student2 = new Student(2L, "Hermione Granger", 17, faculty1);
        List<Student> students = Arrays.asList(student1, student2);

        when(facultyService.getFacultyStudents(1L)).thenReturn(students);

        mockMvc.perform(get("/faculty/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[1].name").value("Hermione Granger"));
    }

    @Test
    void testGetFacultyByName() throws Exception {
        when(facultyService.getFacultyByName("Gryffindor")).thenReturn(faculty1);

        mockMvc.perform(get("/faculty/by-name")
                        .param("name", "Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    void testGetFacultyByName_NotFound() throws Exception {
        when(facultyService.getFacultyByName("Unknown")).thenReturn(null);

        mockMvc.perform(get("/faculty/by-name")
                        .param("name", "Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFaculty() throws Exception {
        Faculty newFaculty = new Faculty(null, "Hufflepuff", "yellow");
        Faculty savedFaculty = new Faculty(4L, "Hufflepuff", "yellow");

        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(savedFaculty);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Hufflepuff"))
                .andExpect(jsonPath("$.color").value("yellow"));
    }

    @Test
    void testEditFaculty() throws Exception {
        Faculty updatedFaculty = new Faculty(1L, "Gryffindor Updated", "scarlet");
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor Updated"))
                .andExpect(jsonPath("$.color").value("scarlet"));
    }

    @Test
    void testEditFacultyById() throws Exception {
        Faculty updatedFaculty = new Faculty(1L, "Gryffindor Updated", "scarlet");
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(put("/faculty/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor Updated"))
                .andExpect(jsonPath("$.color").value("scarlet"));
    }

    @Test
    void testEditFaculty_NotFound() throws Exception {
        Faculty facultyToUpdate = new Faculty(999L, "Unknown", "black");
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(null);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateFacultyColor() throws Exception {
        Faculty updatedFaculty = new Faculty(1L, "Gryffindor", "gold");
        when(facultyService.getFacultyById(1L)).thenReturn(faculty1);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(patch("/faculty/1/color")
                        .param("color", "gold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("gold"));
    }

    @Test
    void testUpdateFacultyName() throws Exception {
        Faculty updatedFaculty = new Faculty(1L, "Gryffindor House", "red");
        when(facultyService.getFacultyById(1L)).thenReturn(faculty1);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(patch("/faculty/1/name")
                        .param("name", "Gryffindor House"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor House"));
    }

    @Test
    void testDeleteFaculty() throws Exception {
        when(facultyService.getFacultyById(1L)).thenReturn(faculty1);

        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteFaculty_NotFound() throws Exception {
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        mockMvc.perform(delete("/faculty/999"))
                .andExpect(status().isNotFound());
    }
}