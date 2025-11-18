package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de créer le répertoire de stockage des fichiers", ex);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        // Valider le nom du fichier
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Le nom du fichier contient un chemin invalide: " + originalFilename);
        }

        // Générer un nom unique pour éviter les collisions
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Créer le sous-répertoire si nécessaire
            Path targetLocation = this.fileStorageLocation;
            if (subdirectory != null && !subdirectory.isEmpty()) {
                targetLocation = this.fileStorageLocation.resolve(subdirectory);
                Files.createDirectories(targetLocation);
            }

            // Copier le fichier vers la destination
            Path targetPath = targetLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif
            if (subdirectory != null && !subdirectory.isEmpty()) {
                return subdirectory + "/" + newFilename;
            }
            return newFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + originalFilename, ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Fichier non trouvé: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Fichier non trouvé: " + filePath, ex);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            Path path = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de supprimer le fichier: " + filePath, ex);
        }
    }

    public String getContentType(String filePath) {
        try {
            Path path = this.fileStorageLocation.resolve(filePath).normalize();
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }
}
