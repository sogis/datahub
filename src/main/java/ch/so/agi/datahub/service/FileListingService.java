package ch.so.agi.datahub.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.so.agi.datahub.model.FileEntry;

@Service
public class FileListingService {
    private final Path rootPath;

    public FileListingService(@Value("${app.targetDirectory}") String targetDirectory) {
        this.rootPath = Paths.get(targetDirectory).toAbsolutePath().normalize();
    }

    public List<FileEntry> listEntries(String relativePath) {
        Path directory = resolveDirectory(relativePath);
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .map(this::toEntry)
                    .sorted(Comparator
                            .comparing(FileEntry::directory).reversed()
                            .thenComparing(entry -> entry.name().toLowerCase()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list files in directory " + directory, e);
        }
    }

    public String parentPath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        Path parent = Paths.get(relativePath).getParent();
        if (parent == null) {
            return "";
        }
        return parent.toString().replace("\\", "/");
    }

    private Path resolveDirectory(String relativePath) {
        Path resolved = rootPath;
        if (relativePath != null && !relativePath.isBlank()) {
            resolved = rootPath.resolve(relativePath).normalize();
        }
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid path outside root directory");
        }
        if (!Files.isDirectory(resolved)) {
            throw new IllegalArgumentException("Path is not a directory");
        }
        return resolved;
    }

    private FileEntry toEntry(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            boolean isDir = attrs.isDirectory();
            long size = isDir ? 0 : attrs.size();
            Instant modified = attrs.lastModifiedTime().toInstant();
            String rel = rootPath.relativize(path).toString().replace("\\", "/");
            return new FileEntry(
                    path.getFileName().toString(),
                    rel,
                    isDir,
                    size,
                    modified
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file attributes for " + path, e);
        }
    }
}
