package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);
    private static final String AVATARS_DIR = "avatars";
    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    public AvatarService(AvatarRepository avatarRepository,
                         StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
        createAvatarsDirectory();
    }

    private void createAvatarsDirectory() {
        try {
            Files.createDirectories(Path.of(AVATARS_DIR));
            logger.info("Avatars directory created: {}", AVATARS_DIR);
        } catch (IOException e) {
            logger.error("Failed to create avatars directory: {}", e.getMessage());
        }
    }

    public Avatar uploadAvatar(Long studentId, MultipartFile avatarFile) throws IOException {
        logger.info("Was invoked method for upload avatar for student ID: {}", studentId);

        if (avatarFile.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size: " + MAX_FILE_SIZE + " bytes");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        String studentDir = AVATARS_DIR + "/student_" + studentId;
        Files.createDirectories(Path.of(studentDir));

        String originalFilename = avatarFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = "avatar_" + System.currentTimeMillis() + fileExtension;
        Path filePath = Path.of(studentDir, filename);

        try (InputStream inputStream = avatarFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        Avatar avatar = findAvatarByStudentId(studentId).orElse(new Avatar());
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
        avatar.setStudent(student);

        Avatar savedAvatar = avatarRepository.save(avatar);
        logger.info("Avatar uploaded successfully for student ID: {}. Avatar ID: {}",
                studentId, savedAvatar.getId());

        return savedAvatar;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public Optional<Avatar> findAvatarByStudentId(Long studentId) {
        logger.info("Was invoked method for find avatar by student ID: {}", studentId);
        return avatarRepository.findByStudentId(studentId);
    }

    public List<Avatar> getAllAvatars() {
        logger.info("Was invoked method for get all avatars without pagination");
        List<Avatar> avatars = avatarRepository.findAll();
        logger.debug("Retrieved {} avatars total", avatars.size());
        return avatars;
    }

    public boolean deleteAvatarByStudentId(Long studentId) {
        logger.info("Was invoked method for delete avatar by student ID: {}", studentId);

        Optional<Avatar> avatarOptional = avatarRepository.findByStudentId(studentId);
        if (avatarOptional.isEmpty()) {
            logger.warn("Avatar not found for student ID: {}", studentId);
            return false;
        }

        Avatar avatar = avatarOptional.get();

        try {
            Files.deleteIfExists(Path.of(avatar.getFilePath()));
            logger.debug("Avatar file deleted: {}", avatar.getFilePath());
        } catch (IOException e) {
            logger.warn("Failed to delete avatar file: {}. Error: {}", avatar.getFilePath(), e.getMessage());
        }

        avatarRepository.delete(avatar);
        logger.info("Avatar deleted successfully for student ID: {}", studentId);
        return true;
    }

    public boolean existsByStudentId(Long studentId) {
        logger.debug("Checking if avatar exists for student ID: {}", studentId);
        boolean exists = avatarRepository.findByStudentId(studentId).isPresent();
        logger.debug("Avatar exists for student ID {}: {}", studentId, exists);
        return exists;
    }

    public Optional<Avatar> findById(Long id) {
        logger.info("Was invoked method for find avatar by ID: {}", id);
        return avatarRepository.findById(id);
    }

    public static class PaginationInfo {
        private final int currentPage;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;

        public PaginationInfo(int currentPage, int pageSize, long totalElements,
                              int totalPages, boolean first, boolean last) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }

        public int getCurrentPage() { return currentPage; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean isFirst() { return first; }
        public boolean isLast() { return last; }

        @Override
        public String toString() {
            return String.format(
                    "PaginationInfo{page=%d, size=%d, totalElements=%d, totalPages=%d, first=%s, last=%s}",
                    currentPage, pageSize, totalElements, totalPages, first, last
            );
        }
    }
}