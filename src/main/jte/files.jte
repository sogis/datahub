@param ch.so.agi.datahub.model.FileListingViewModel model
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Datahub - Files</title>
    <style>
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell,
                'Open Sans', 'Helvetica Neue', sans-serif;
            font-size: 14px;
            margin: 20px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 8px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #f2f2f2;
            cursor: pointer;
            user-select: none;
        }
        tr:hover {
            background-color: #fafafa;
        }
        a:link,
        a:visited {
            color: rgb(21, 101, 192);
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
        .breadcrumbs {
            margin-bottom: 16px;
            font-size: 13px;
        }
        .dir-badge {
            display: inline-block;
            padding: 2px 6px;
            background-color: #e3f2fd;
            color: #1565c0;
            border-radius: 3px;
            font-size: 11px;
            margin-right: 6px;
        }
    </style>
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
                            ${entry.name()}
                        @endif
                    </td>
                    <td data-value="${entry.directory() ? \"dir\" : \"file\"}">
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

    <script>
        const table = document.getElementById('fileTable');
        const headers = Array.from(table.querySelectorAll('th'));

        headers.forEach((header, index) => {
            header.addEventListener('click', () => {
                const tbody = table.querySelector('tbody');
                const rows = Array.from(tbody.querySelectorAll('tr'));
                const type = header.dataset.type || 'string';
                const direction = header.dataset.direction === 'asc' ? 'desc' : 'asc';
                headers.forEach(h => h.removeAttribute('data-direction'));
                header.dataset.direction = direction;

                rows.sort((rowA, rowB) => {
                    const cellA = rowA.children[index];
                    const cellB = rowB.children[index];
                    const valueA = cellA.dataset.value || '';
                    const valueB = cellB.dataset.value || '';
                    if (type === 'number') {
                        const numA = Number(valueA);
                        const numB = Number(valueB);
                        return direction === 'asc' ? numA - numB : numB - numA;
                    }
                    return direction === 'asc'
                        ? valueA.localeCompare(valueB)
                        : valueB.localeCompare(valueA);
                });

                rows.forEach(row => tbody.appendChild(row));
            });
        });
    </script>
</body>
</html>
