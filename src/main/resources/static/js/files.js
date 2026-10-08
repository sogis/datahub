
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
    