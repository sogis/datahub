package ch.so.agi.datahub.model;

import java.util.List;

public record FileListingViewModel(
        String currentPath,
        String parentPath,
        String tokenQueryParam,
        List<FileListingViewEntry> entries
) {
}
