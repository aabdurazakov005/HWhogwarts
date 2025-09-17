package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);
    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB
    private static final int PREVIEW_SIZE = 100; // 100x100 px preview

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;
    private final String avatarsDir;

    public AvatarService(AvatarRepository avatarRepository,
                         StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
        this.avatarsDir = "avatars";
        createAvatarsDirectory();
    }

    private void createAvatarsDirectory() {
        try {
            Files.createDirectories(Path.of(avatarsDir));
            logger.info("Avatars directory created: {}", avatarsDir);
        } catch (IOException e) {
            logger.error("Failed to create avatars directory", e);
        }
    }

    public boolean existsByStudentId(Long studentId) {
        return avatarRepository.findByStudentId(studentId).isPresent();
    }

    public Optional<Avatar> findById(Long id) {
        return avatarRepository.findById(id);
    }

    public void deleteById(Long id) {
        Optional<Avatar> avatarOptional = avatarRepository.findById(id);
        if (avatarOptional.isPresent()) {
            Avatar avatar = avatarOptional.get();
            try {
                Files.deleteIfExists(Path.of(avatar.getFilePath()));
            } catch (IOException e) {
                logger.warn("Failed to delete avatar file: {}", avatar.getFilePath(), e);
            }

            avatarRepository.deleteById(id);
            logger.info("Avatar deleted with ID: {}", id);
        }
    }
}