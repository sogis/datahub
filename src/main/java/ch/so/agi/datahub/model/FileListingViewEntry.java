package ch.so.agi.datahub.model;

public record FileListingViewEntry(
        String name,
        String encodedRelativePath,
        boolean directory,
        String sizeDisplay,
        long sizeSort,
        String modifiedDisplay,
        long modifiedEpoch
) {
}
