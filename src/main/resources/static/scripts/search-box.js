(() => {
    const elements = {
        box: document.getElementById("memoirSearch"),
        trigger: document.getElementById("memoirSearchTrigger"),
        input: document.getElementById('memoirSearchInput'),
        dropMenu: document.getElementById('memoirSearchDropdown'),
        dropBody: document.getElementById('memoirDropBody'),
        footer: document.getElementById('memoirDropFooter'),
        seeAllLink: document.getElementById('memoirSeeAll'),
        queryLabel: document.getElementById('memoirQueryLabel'),
    }

    if(!elements.box || !elements.input) {
        console.warn('[memoir-search] elements not found');
        return;
    }

    const bsDropdown = new bootstrap.Dropdown(elements.trigger, {autoClose: true});
    let debounceTimer = null;
    let abortController = null;

    const escHtml = (str) => String(str || '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');

    const initials = (first = '', last = '') => (first.charAt(0) + last.charAt(0)).toUpperCase();

    const avatarHTML = ({ profileImage, firstname, lastname }) => {
        if (profileImage) {
            return `<div class="memoir-drop-avatar"><img src="${escHtml(profileImage)}" alt=""/></div>`;
        }
        return `<div class="memoir-drop-avatar">${escHtml(initials(firstname, lastname))}</div>`;
    };

    const createRow = (href, avatarContent, name, sub) => `
        <a class="memoir-drop-row dropdown-item" href="${href}">
            ${avatarContent}
            <div>
                <div class="memoir-drop-name">${escHtml(name)}</div>
                <div class="memoir-drop-sub">${escHtml(sub)}</div>
            </div>
        </a>`;

    const renderResults = (data, query) => {
        const { users = [], groups = [], events = [] } = data;

        const sortedUsers = [...users]

        if (!sortedUsers.length) {
            elements.dropBody.innerHTML = `<span class="dropdown-item-text text-muted small">No results for &ldquo;${escHtml(query)}&rdquo;</span>`;
            elements.footer.style.display = 'none';
            return;
        }

        let sections = [];

        if (sortedUsers.length) {
            const userHtml = sortedUsers.slice(0, 3).map(u =>
                createRow(`/profile/${u.username}`, avatarHTML(u), `${u.firstname} ${u.lastname}`, `@${u.username}`)
            ).join('');
            sections.push(`<div class="memoir-drop-section-label">People</div>${userHtml}`);
        }


        elements.dropBody.innerHTML = sections.join('<hr class="dropdown-divider">');
        elements.queryLabel.textContent = query;
        elements.seeAllLink.href = `/search?q=${encodeURIComponent(query)}`;
        elements.footer.style.display = 'block';
    };
    elements.trigger.addEventListener('click', () => {
        elements.input.focus();
        bsDropdown.show();
    });

    elements.input.addEventListener('focus', () => bsDropdown.show());

    elements.input.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') { bsDropdown.hide(); elements.input.blur(); }
        if (e.key === 'Enter' && elements.input.value.trim()) {
            window.location.href = `/search?q=${encodeURIComponent(elements.input.value.trim())}`;
        }
    });

    elements.input.addEventListener('input', async () => {
        const q = elements.input.value.trim();

        if (!q) {
            elements.footer.style.display = 'none';
            elements.dropBody.innerHTML = '<span class="dropdown-item-text text-muted small">Start typing to search\u2026</span>';
            return;
        }

        elements.queryLabel.textContent = q;
        elements.footer.style.display = 'block';

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async () => {
            if (abortController) abortController.abort();
            abortController = new AbortController();

            // elements.dropBody.innerHTML = '<span class="dropdown-item-text text-muted small">Searching\u2026</span>';

            try {
                const response = await fetch(`/search?q=${encodeURIComponent(q)}`, {
                    headers: { 'Accept': 'application/json' },
                    signal: abortController.signal
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const data = await response.json();
                renderResults(data, q);
            } catch (err) {
                if (err.name !== 'AbortError') {
                    console.warn('[memoir-search] fetch failed:', err);
                }
            }
        }, 250);
    });
})();