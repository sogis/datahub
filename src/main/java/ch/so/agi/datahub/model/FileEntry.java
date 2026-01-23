package ch.so.agi.datahub.model;

import java.time.Instant;

public record FileEntry(
        String name,
        String relativePath,
        boolean directory,
        long size,
        Instant modified
) {
}
