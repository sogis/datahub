package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class DeliveryFiles {
  private final AppProperties properties;

  public DeliveryFiles(AppProperties properties) {
    this.properties = properties;
  }

  public void saveInput(UUID id, InputStream input) throws IOException {
    try (input) {
      var path = input(id);
      Files.createDirectory(path.getParent());
      atomicCopy(input, path);
      sync(path.getParent().getParent());
    }
  }

  public Path input(UUID id) throws IOException {
    return resolve(properties.getWorkDirectory(), properties.getFolderPrefix() + id, "input.xtf");
  }

  public Path log(UUID id) throws IOException {
    return resolve(properties.getWorkDirectory(), properties.getFolderPrefix() + id, id + ".log");
  }

  public Path legacyLog(UUID id, String relative) throws IOException {
    if (!relative.matches("attempts/[0-9a-f-]{36}-[0-9]+/result\\.log"))
      throw new IOException("Invalid legacy log reference");
    Path directory = input(id).getParent();
    Path result = directory.resolve(relative);
    for (Path path = result; !path.equals(directory); path = path.getParent())
      if (Files.isSymbolicLink(path)) throw new IOException("Symbolic links are not permitted");
    return result;
  }

  public void delete(UUID id) throws IOException {
    FileSystemUtils.deleteRecursively(input(id).getParent());
  }

  void publish(UUID id, String theme, String operat) throws IOException {
    var file = resolve(properties.getTargetDirectory(), theme, operat + ".xtf");
    var result = resolve(properties.getTargetDirectory(), theme, operat + ".xtf.log");
    Files.createDirectories(file.getParent());
    var directory = file.getParent();
    try (var stagedInput = stage(input(id), directory);
         var stagedLog = stage(log(id), directory);
         var previousLog = Files.isRegularFile(result, LinkOption.NOFOLLOW_LINKS) ? stage(result, directory) : null) {
      replace(stagedLog, result);
      try {
        replace(stagedInput, file);
      } catch (IOException failure) {
        try {
          if (previousLog == null) Files.deleteIfExists(result);
          else replace(previousLog, result);
          sync(directory);
        } catch (IOException rollback) {
          failure.addSuppressed(rollback);
        }
        throw failure;
      }
      sync(directory);
    }
    sync(directory.getParent());
  }

  static void atomicCopy(InputStream source, Path target) throws IOException {
    try (var staged = stage(source, target.getParent())) {
      replace(staged, target);
      sync(target.getParent());
    }
  }

  private static StagedFile stage(Path source, Path directory) throws IOException {
    try (var input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS)) {
      return stage(input, directory);
    }
  }

  private static StagedFile stage(InputStream source, Path directory) throws IOException {
    var temp = Files.createTempFile(directory, ".datahub-", ".tmp");
    try {
      try (var output = Files.newOutputStream(temp)) {
        source.transferTo(output);
      }
      if (temp.getFileSystem().supportedFileAttributeViews().contains("posix"))
        Files.setPosixFilePermissions(temp, PosixFilePermissions.fromString("rw-rw-r--"));
      sync(temp);
      return new StagedFile(temp);
    } catch (IOException | RuntimeException failure) {
      try {
        Files.deleteIfExists(temp);
      } catch (IOException cleanup) {
        failure.addSuppressed(cleanup);
      }
      throw failure;
    }
  }

  private static void replace(StagedFile staged, Path target) throws IOException {
    Files.move(staged.path(), target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
  }

  private record StagedFile(Path path) implements AutoCloseable {
    @Override
    public void close() throws IOException {
      Files.deleteIfExists(path);
    }
  }

  private static void sync(Path path) throws IOException {
    try (var channel = FileChannel.open(path,
        Files.isDirectory(path) ? StandardOpenOption.READ : StandardOpenOption.WRITE)) {
      channel.force(true);
    }
  }

  static void segment(String value) {
    if (value == null || value.isBlank() || value.equals(".") || value.equals("..")
        || value.contains("/") || value.contains("\\")
        || value.codePoints().anyMatch(Character::isISOControl))
      throw new IllegalArgumentException("Invalid file path segment");
  }

  public static Path resolve(String root, String directory, String file) throws IOException {
    segment(directory);
    segment(file);
    Path base = Path.of(root).toAbsolutePath().normalize();
    Files.createDirectories(base);
    base = base.toRealPath();
    Path dir = base.resolve(directory);
    Path path = dir.resolve(file);
    if (Files.isSymbolicLink(dir) || Files.isSymbolicLink(path))
      throw new IOException("Symbolic links are not permitted");
    return path;
  }
}
