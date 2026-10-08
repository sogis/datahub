package ch.so.agi.datahub.service;

import java.io.*;
import java.nio.file.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;

@Service
public class LocalFilesStorageService implements FilesStorageService {
  public static void segment(String value) {
    if (value == null
        || value.isBlank()
        || value.equals(".")
        || value.equals("..")
        || value.contains("/")
        || value.contains("\\")
        || value.indexOf(0) >= 0) throw new IllegalArgumentException("Invalid file path segment");
  }

  public static Path resolve(String root, String directory, String file) throws IOException {
    segment(directory);
    segment(file);
    Path base = Path.of(root).toAbsolutePath().normalize();
    Files.createDirectories(base);
    base = base.toRealPath();
    Path dir = base.resolve(directory);
    if (Files.isSymbolicLink(dir)) throw new IOException("Symlink directory not permitted");
    Path path = dir.resolve(file);
    if (Files.isSymbolicLink(path)) throw new IOException("Symlink file not permitted");
    return path;
  }

  @Override
  public void init() {}

  @Override
  public void save(InputStream input, String filename, String parent, String prefix, String root)
      throws IOException {
    Path path = resolve(root, (prefix == null ? "" : prefix) + parent, filename);
    Files.createDirectories(path.getParent());
    Path temp = Files.createTempFile(path.getParent(), "copy-", ".tmp");
    try (input) {
      Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
      LocalArtifactStore.force(temp);
      Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temp);
    }
  }

  @Override
  public Resource load(String filename, String parent, String prefix, String root)
      throws IOException {
    Path path = resolve(root, (prefix == null ? "" : prefix) + parent, filename);
    if (!Files.isRegularFile(path) || !Files.isReadable(path))
      throw new FileNotFoundException("Artifact is not available");
    return new FileSystemResource(path);
  }
}
