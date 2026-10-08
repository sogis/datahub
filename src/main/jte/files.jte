@param ch.so.agi.datahub.model.FileListingViewModel model
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datahub - Files</title>
    <link rel="stylesheet" href="/css/files.css">
</head>
<body>
    <h2>Dateien</h2>

    <div class="breadcrumbs">
        <strong>Pfad:</strong>
        @if(model.currentPath() == null || model.currentPath().isBlank())
            /
        @else
            ${model.currentPath()}
        @endif
        @if(model.parentPath() != null)
            &nbsp;|&nbsp;
            <a href="/ui/files?path=${model.parentPath()}${model.tokenQueryParam()}">Eine Ebene hoch</a>
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
            @for (var entry : model.entries())
                <tr>
                    <td data-value="${entry.name().toLowerCase()}">
                        @if(entry.directory())
                            <span class="dir-badge">DIR</span>
                            <a href="/ui/files?path=${entry.encodedRelativePath()}${model.tokenQueryParam()}">${entry.name()}</a>
                        @else
                            <a href="/ui/files/download?path=${entry.encodedRelativePath()}${model.tokenQueryParam()}">${entry.name()}</a>
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

    <script src="/js/files.js" defer></script>
</body>
</html>
