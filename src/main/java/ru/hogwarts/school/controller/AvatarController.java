package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/avatar")
public class AvatarController {

    private static final Logger logger = LoggerFactory.getLogger(AvatarController.class);
    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping
    public ResponseEntity<List<Avatar>> getAllAvatars() {
        logger.info("Was invoked GET method to get all avatars");

        try {
            List<Avatar> avatars = avatarService.getAllAvatars();
            logger.debug("Retrieved {} avatars total", avatars.size());
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            logger.error("Error retrieving all avatars: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avatar> getAvatarById(@PathVariable Long id) {
        logger.info("Was invoked GET method to get avatar by ID: {}", id);

        Optional<Avatar> avatar = avatarService.findById(id);
        if (avatar.isEmpty()) {
            logger.warn("Avatar not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning avatar with ID: {}", id);
        return ResponseEntity.ok(avatar.get());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Avatar> getAvatarByStudentId(@PathVariable Long studentId) {
        logger.info("Was invoked GET method to get avatar by student ID: {}", studentId);

        Optional<Avatar> avatar = avatarService.findAvatarByStudentId(studentId);
        if (avatar.isEmpty()) {
            logger.info("Avatar not found for student ID: {}", studentId);
            return ResponseEntity.notFound().build();
        }

        logger.debug("Returning avatar for student ID: {}", studentId);
        return ResponseEntity.ok(avatar.get());
    }

    @PostMapping("/{studentId}")
    public ResponseEntity<String> uploadAvatar(
            @PathVariable Long studentId,
            @RequestParam MultipartFile avatar) {
        logger.info("Was invoked POST method to upload avatar for student ID: {}", studentId);
        logger.debug("Avatar file: name={}, size={} bytes, type={}",
                avatar.getOriginalFilename(),
                avatar.getSize(),
                avatar.getContentType());

        if (avatar.isEmpty()) {
            logger.warn("Empty avatar file provided for student ID: {}", studentId);
            return ResponseEntity.badRequest().body("Avatar file is empty");
        }

        if (!isSupportedContentType(avatar.getContentType())) {
            logger.warn("Unsupported file type: {} for student ID: {}", avatar.getContentType(), studentId);
            return ResponseEntity.badRequest().body("Unsupported file type. Only images are allowed.");
        }

        try {
            Avatar savedAvatar = avatarService.uploadAvatar(studentId, avatar);
            logger.info("Avatar uploaded successfully for student ID: {}. Avatar ID: {}",
                    studentId, savedAvatar.getId());
            return ResponseEntity.ok("Avatar uploaded successfully. Avatar ID: " + savedAvatar.getId());
        } catch (IOException e) {
            logger.error("Error uploading avatar for student ID {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading avatar: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid student ID for avatar upload: {}. Error: {}", studentId, e.getMessage());
            return ResponseEntity.badRequest().body("Student not found with ID: " + studentId);
        } catch (Exception e) {
            logger.error("Unexpected error uploading avatar for student ID {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @DeleteMapping("/student/{studentId}")
    public ResponseEntity<String> deleteAvatarByStudentId(@PathVariable Long studentId) {
        logger.info("Was invoked DELETE method to delete avatar by student ID: {}", studentId);

        boolean deleted = avatarService.deleteAvatarByStudentId(studentId);
        if (!deleted) {
            logger.warn("Avatar not found for student ID: {}", studentId);
            return ResponseEntity.notFound().build();
        }

        logger.info("Avatar deleted successfully for student ID: {}", studentId);
        return ResponseEntity.ok("Avatar deleted successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAvatarById(@PathVariable Long id) {
        logger.info("Was invoked DELETE method to delete avatar by ID: {}", id);

        Optional<Avatar> avatar = avatarService.findById(id);
        if (avatar.isEmpty()) {
            logger.warn("Avatar not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        boolean deleted = avatarService.deleteAvatarByStudentId(avatar.get().getStudent().getId());
        if (!deleted) {
            logger.error("Failed to delete avatar with ID: {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        logger.info("Avatar deleted successfully with ID: {}", id);
        return ResponseEntity.ok("Avatar deleted successfully");
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAvatarsCount() {
        logger.info("Was invoked GET method to get avatars count");

        try {
            List<Avatar> avatars = avatarService.getAllAvatars();
            long count = avatars.size();
            logger.debug("Total avatars count: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting avatars count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/student/{studentId}")
    public ResponseEntity<Boolean> checkAvatarExists(@PathVariable Long studentId) {
        logger.info("Was invoked GET method to check if avatar exists for student ID: {}", studentId);

        boolean exists = avatarService.existsByStudentId(studentId);
        logger.debug("Avatar exists for student ID {}: {}", studentId, exists);
        return ResponseEntity.ok(exists);
    }


    private boolean isSupportedContentType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp") ||
                        contentType.equals("image/bmp")
        );
    }

    public static class AvatarStats {
        private final long totalAvatars;
        private final long totalFileSize;
        private final double averageFileSize;
        private final String mostCommonMediaType;

        public AvatarStats(long totalAvatars, long totalFileSize, double averageFileSize, String mostCommonMediaType) {
            this.totalAvatars = totalAvatars;
            this.totalFileSize = totalFileSize;
            this.averageFileSize = averageFileSize;
            this.mostCommonMediaType = mostCommonMediaType;
        }

        public long getTotalAvatars() { return totalAvatars; }
        public long getTotalFileSize() { return totalFileSize; }
        public double getAverageFileSize() { return averageFileSize; }
        public String getMostCommonMediaType() { return mostCommonMediaType; }

        @Override
        public String toString() {
            return String.format(
                    "AvatarStats{totalAvatars=%d, totalFileSize=%d, averageFileSize=%.2f, mostCommonMediaType='%s'}",
                    totalAvatars, totalFileSize, averageFileSize, mostCommonMediaType
            );
        }
    }
}