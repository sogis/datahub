@param String currentPath
@param String parentPath
@param java.util.List<ch.so.agi.datahub.model.FileListingViewEntry> entries
@param String context
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datahub - Files</title>
    <link rel="stylesheet" href="${context}/css/files.css">
</head>
<body>
    <h2>Dateien</h2>

    <div class="breadcrumbs">
        <strong>Pfad:</strong>
        @if(currentPath == null || currentPath.isBlank())
            /
        @else
            ${currentPath}
        @endif
        @if(parentPath != null)
            &nbsp;|&nbsp;
            <a href="${context}/ui/files?path=${parentPath}">Eine Ebene hoch</a>
        @endif
    </div>

    <table id="fileTable">
        <thead>
            <tr>
                <th data-type="string">Name</th>
                <th data-type="string">Typ</th>
                <th data-type="number">Grösse</th>
                <th data-type="number">Geändert</th>
            </tr>
        </thead>
        <tbody>
            @for (var entry : entries)
                <tr>
                    <td data-value="${entry.name().toLowerCase()}">
                        @if(entry.directory())
                            <span class="dir-badge">DIR</span>
                            <a href="${context}/ui/files?path=${entry.encodedRelativePath()}">${entry.name()}</a>
                        @else
                            <a href="${context}/ui/files/download?path=${entry.encodedRelativePath()}">${entry.name()}</a>
                        @endif
                    </td>
                    <td data-value='${entry.directory() ? "dir" : "file"}'>
                        ${entry.directory() ? "Ordner" : "Datei"}
                    </td>
                    <td data-value="${entry.sizeSort()}">
                        ${entry.sizeDisplay()}
                    </td>
                    <td data-value="${entry.modifiedEpoch()}">
                        ${entry.modifiedDisplay()}
                    </td>
                </tr>
            @endfor
        </tbody>
    </table>

    <script src="${context}/js/files.js" defer></script>
</body>
</html>
