package ch.so.agi.datahub.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public interface ArtifactStore {
  void saveInput(UUID deliveryId, InputStream input) throws IOException;

  Path materialize(UUID deliveryId, UUID attemptId, long version) throws IOException;

  void saveResult(UUID deliveryId, UUID attemptId, long version, Path log) throws IOException;

  Path log(UUID deliveryId, UUID attemptId, long version) throws IOException;

  Path input(UUID deliveryId) throws IOException;

  void delete(UUID deliveryId) throws IOException;
}
