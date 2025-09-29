package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTestWithWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Faculty gryffindor = new Faculty(1L, "Gryffindor", "red");
    private final Faculty slytherin = new Faculty(2L, "Slytherin", "green");

    private final Student student1 = new Student(1L, "Harry Potter", 17, gryffindor);
    private final Student student2 = new Student(2L, "Hermione Granger", 17, gryffindor);
    private final Student student3 = new Student(3L, "Ron Weasley", 16, gryffindor);
    private final Student student4 = new Student(4L, "Draco Malfoy", 17, slytherin);

    @Test
    void testGetStudentInfo() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(student1);

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void testGetStudentInfo_NotFound() throws Exception {
        when(studentService.getStudentById(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindStudentsByAge() throws Exception {
        List<Student> studentsAge17 = Arrays.asList(student1, student2, student4);
        when(studentService.getStudentsByAge(17)).thenReturn(studentsAge17);

        mockMvc.perform(get("/student")
                        .param("age", "17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[1].name").value("Hermione Granger"))
                .andExpect(jsonPath("$[2].name").value("Draco Malfoy"));
    }

    @Test
    void testFindStudents_NoAgeParam() throws Exception {
        List<Student> allStudents = Arrays.asList(student1, student2, student3, student4);
        when(studentService.getAllStudents()).thenReturn(allStudents);

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void testGetStudentsByAgeBetween() throws Exception {
        List<Student> studentsBetween16And17 = Arrays.asList(student1, student2, student3, student4);
        when(studentService.getStudentsByAgeBetween(16, 17)).thenReturn(studentsBetween16And17);

        mockMvc.perform(get("/student/by-age-between")
                        .param("minAge", "16")
                        .param("maxAge", "17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void testGetTotalStudentCount() throws Exception {
        // Given
        when(studentService.getTotalCountOfStudents()).thenReturn(4);

        // When & Then
        mockMvc.perform(get("/student/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("4"));
    }

    @Test
    void testGetAverageStudentAge() throws Exception {
        when(studentService.getAverageAgeOfStudents()).thenReturn(16.75);

        mockMvc.perform(get("/student/average-age"))
                .andExpect(status().isOk())
                .andExpect(content().string("16.75"));
    }

    @Test
    void testGetLastFiveStudents() throws Exception {
        List<Student> lastFiveStudents = Arrays.asList(student4, student3, student2, student1);
        when(studentService.getLastFiveStudents()).thenReturn(lastFiveStudents);

        mockMvc.perform(get("/student/last-five"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].name").value("Draco Malfoy"))
                .andExpect(jsonPath("$[3].name").value("Harry Potter"));
    }

    @Test
    void testGetStudentFaculty() throws Exception {
        when(studentService.getStudentFaculty(1L)).thenReturn(gryffindor);

        mockMvc.perform(get("/student/1/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    void testGetStudentFaculty_NotFound() throws Exception {
        when(studentService.getStudentFaculty(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/faculty"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateStudent() throws Exception {
        Student newStudent = new Student(null, "Luna Lovegood", 15, null);
        Student savedStudent = new Student(5L, "Luna Lovegood", 15, null);

        when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Luna Lovegood"))
                .andExpect(jsonPath("$.age").value(15));
    }

    @Test
    void testEditStudent() throws Exception {
        Student updatedStudent = new Student(1L, "Harry Potter", 18, gryffindor);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void testEditStudentById() throws Exception {
        Student updatedStudent = new Student(1L, "Harry Potter", 18, gryffindor);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/student/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void testEditStudent_NotFound() throws Exception {
        Student studentToUpdate = new Student(999L, "Unknown Student", 20, null);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteStudent() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(student1);

        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateStudentAge() throws Exception {
        Student updatedStudent = new Student(1L, "Harry Potter", 18, gryffindor);
        when(studentService.getStudentById(1L)).thenReturn(student1);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(patch("/student/1/age")
                        .param("age", "18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void testUpdateStudentAge_InvalidAge() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(student1);

        mockMvc.perform(patch("/student/1/age")
                        .param("age", "15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchStudentsByName() throws Exception {
        List<Student> foundStudents = Collections.singletonList(student1);
        when(studentService.findStudentsByName("Harry")).thenReturn(foundStudents);

        mockMvc.perform(get("/student/search")
                        .param("name", "Harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }

    @Test
    void testSearchStudentsByName_Empty() throws Exception {
        when(studentService.findStudentsByName("Unknown")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/search")
                        .param("name", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetStudentsByFaculty() throws Exception {
        List<Student> gryffindorStudents = Arrays.asList(student1, student2, student3);
        when(studentService.getStudentsByFacultyId(1L)).thenReturn(gryffindorStudents);

        mockMvc.perform(get("/student/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[1].name").value("Hermione Granger"))
                .andExpect(jsonPath("$[2].name").value("Ron Weasley"));
    }

    @Test
    void testCreateStudent_ValidationError() throws Exception {
        Student invalidStudent = new Student(null, "", 15, null);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStudentsByAgeBetween_InvalidRange() throws Exception {
        when(studentService.getStudentsByAgeBetween(18, 16))
                .thenThrow(new IllegalArgumentException("minAge cannot be greater than maxAge"));

        mockMvc.perform(get("/student/by-age-between")
                        .param("minAge", "18")
                        .param("maxAge", "16"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllStudents_Empty() throws Exception {
        when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDeleteStudent_NotFound() throws Exception {
        when(studentService.getStudentById(999L)).thenReturn(null);

        mockMvc.perform(delete("/student/999"))
                .andExpect(status().isNotFound());
    }
}