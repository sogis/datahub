package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class DeliveryFiles {
  private static final Logger log = LoggerFactory.getLogger(DeliveryFiles.class);
  private final AppProperties properties;

  public DeliveryFiles(AppProperties properties) {
    this.properties = properties;
  }

  public void saveInput(UUID id, InputStream input) throws IOException {
    try (input) {
      var path = input(id);
      createSharedDirectory(path.getParent());
      try {
        atomicCopy(input, path);
        sync(path.getParent().getParent());
      } catch (IOException | RuntimeException failure) {
        try {
          FileSystemUtils.deleteRecursively(path.getParent());
        } catch (IOException cleanup) {
          failure.addSuppressed(cleanup);
        }
        throw failure;
      }
    }
  }

  public Path input(UUID id) throws IOException {
    return resolve(properties.getWorkDirectory(), properties.getFolderPrefix() + id, "input.xtf");
  }

  public Path log(UUID id) throws IOException {
    return resolve(properties.getWorkDirectory(), properties.getFolderPrefix() + id, id + ".log");
  }

  public void delete(UUID id) throws IOException {
    FileSystemUtils.deleteRecursively(input(id).getParent());
  }

  void publish(UUID id, String theme, String operat) throws IOException {
    var file = resolve(properties.getTargetDirectory(), theme, operat + ".xtf");
    var result = resolve(properties.getTargetDirectory(), theme, operat + ".xtf.log");
    try {
      createSharedDirectory(file.getParent());
    } catch (FileAlreadyExistsException e) {
      if (!Files.isDirectory(file.getParent(), LinkOption.NOFOLLOW_LINKS)) throw e;
    }
    var directory = file.getParent();
    sync(directory.getParent());
    String prefix = ".datahub-" + id + "-";
    try (var stagedInput = stage(input(id), directory, prefix);
         var stagedLog = stage(log(id), directory, prefix);
         var previousInput = backup(file, prefix);
         var previousLog = backup(result, prefix)) {
      replace(stagedLog, result);
      boolean replacedInput = false;
      try {
        replace(stagedInput, file);
        replacedInput = true;
        sync(directory);
      } catch (IOException | RuntimeException failure) {
        if (replacedInput) restore(previousInput, file, failure);
        restore(previousLog, result, failure);
        try {
          sync(directory);
        } catch (IOException | RuntimeException rollback) {
          failure.addSuppressed(rollback);
        }
        throw failure;
      }
    }
  }

  private static StagedFile backup(Path target, String prefix) throws IOException {
    return Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) ? stage(target, target.getParent(), prefix) : null;
  }

  private static void restore(StagedFile previous, Path target, Exception failure) {
    try {
      if (previous == null) Files.deleteIfExists(target);
      else replace(previous, target);
    } catch (IOException | RuntimeException rollback) {
      if (previous != null) {
        previous.keep = true;
        log.error("Could not restore {}; retained backup at {}", target, previous.path);
      }
      failure.addSuppressed(rollback);
    }
  }

  private static void createSharedDirectory(Path path) throws IOException {
    if (path.getFileSystem().supportedFileAttributeViews().contains("posix")) {
      Files.createDirectory(path, PosixFilePermissions.asFileAttribute(
          PosixFilePermissions.fromString("rwxrwxr-x")));
      // Restore group access masked by umask, keeping an inherited setgid bit.
      if (path.getFileSystem().supportedFileAttributeViews().contains("unix")) {
        int mode = (int) Files.getAttribute(path, "unix:mode");
        if ((mode & 0070) != 0070) Files.setAttribute(path, "unix:mode", mode | 0070);
      } else {
        var permissions = Files.getPosixFilePermissions(path);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        Files.setPosixFilePermissions(path, permissions);
      }
    } else {
      Files.createDirectory(path);
    }
  }

  static void atomicCopy(InputStream source, Path target) throws IOException {
    try (var staged = stage(source, target.getParent(), ".datahub-")) {
      replace(staged, target);
      sync(target.getParent());
    }
  }

  private static StagedFile stage(Path source, Path directory, String prefix) throws IOException {
    try (var input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS)) {
      return stage(input, directory, prefix);
    }
  }

  private static StagedFile stage(InputStream source, Path directory, String prefix) throws IOException {
    var temp = Files.createTempFile(directory, prefix, ".tmp");
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
    Files.move(staged.path, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
  }

  private static final class StagedFile implements AutoCloseable {
    private final Path path;
    private boolean keep;

    private StagedFile(Path path) {
      this.path = path;
    }

    @Override
    public void close() {
      if (keep) return;
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        log.warn("Could not remove staging file {}", path, e);
      }
    }
  }

  private static void sync(Path path) throws IOException {
    try (var channel = FileChannel.open(path,
        Files.isDirectory(path) ? StandardOpenOption.READ : StandardOpenOption.WRITE)) {
      channel.force(true);
    }
  }

  public static void publicationSegment(String value) {
    segment(value);
    // Spring Security's HTTP firewall rejects these in download path segments.
    if (value.contains("%") || value.contains(";"))
      throw new IllegalArgumentException("Invalid download path segment");
  }

  private static void segment(String value) {
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
