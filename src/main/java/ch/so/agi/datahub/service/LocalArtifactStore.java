package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class LocalArtifactStore implements ArtifactStore {
  private final AppProperties p;

  public LocalArtifactStore(AppProperties p) {
    this.p = p;
  }

  private Path root() throws IOException {
    var root = Path.of(p.getWorkDirectory()).toAbsolutePath().normalize();
    Files.createDirectories(root);
    return root.toRealPath();
  }

  private Path directory(UUID id) throws IOException {
    return root().resolve(p.getFolderPrefix() + id);
  }

  @Override
  public void saveInput(UUID id, InputStream input) throws IOException {
    var dir = directory(id);
    Files.createDirectories(dir);
    if (Files.isSymbolicLink(dir)) throw new IOException("Invalid artifact directory");
    Path temp = Files.createTempFile(dir, "input-", ".tmp");
    try (input) {
      Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
      force(temp);
      Files.move(temp, dir.resolve("input.xtf"), StandardCopyOption.ATOMIC_MOVE);
    } finally {
      Files.deleteIfExists(temp);
    }
  }

  @Override
  public Path input(UUID id) throws IOException {
    var file = directory(id).resolve("input.xtf");
    check(file);
    return file;
  }

  @Override
  public Path materialize(UUID delivery, UUID attempt, long version) throws IOException {
    Path workspace = root().resolve("workspaces").resolve(attempt + "-" + version);
    Files.createDirectories(workspace);
    check(workspace);
    Files.copy(
        input(delivery), workspace.resolve("input.xtf"), StandardCopyOption.REPLACE_EXISTING);
    return workspace.resolve("input.xtf");
  }

  @Override
  public void saveResult(UUID delivery, UUID attempt, long version, Path log) throws IOException {
    Path dir = directory(delivery).resolve("attempts").resolve(attempt + "-" + version);
    Files.createDirectories(dir);
    check(dir);
    Path target = dir.resolve("result.log");
    if (Files.exists(target)) throw new IOException("Result already exists");
    Path temp = Files.createTempFile(dir, "result-", ".tmp");
    try {
      Files.copy(log, temp, StandardCopyOption.REPLACE_EXISTING);
      force(temp);
      Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
    } finally {
      Files.deleteIfExists(temp);
    }
  }

  @Override
  public Path log(UUID delivery, UUID attempt, long version) throws IOException {
    var path =
        directory(delivery)
            .resolve("attempts")
            .resolve(attempt + "-" + version)
            .resolve("result.log");
    check(path);
    return path;
  }

  @Override
  public void delete(UUID delivery) throws IOException {
    var dir = directory(delivery);
    if (Files.exists(dir)) {
      check(dir);
      FileSystemUtils.deleteRecursively(dir);
    }
  }

  private void check(Path path) throws IOException {
    var realRoot = root();
    Path normalized = path.toAbsolutePath().normalize();
    if (!normalized.startsWith(realRoot)) throw new IOException("Path outside artifact root");
    for (Path cursor = normalized;
        cursor != null && !cursor.equals(realRoot);
        cursor = cursor.getParent())
      if (Files.isSymbolicLink(cursor)) throw new IOException("Symlinks are not artifact paths");
  }

  static void force(Path path) throws IOException {
    try (var channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
      channel.force(true);
    }
  }
}
