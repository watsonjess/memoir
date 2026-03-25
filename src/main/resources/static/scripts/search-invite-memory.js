(() => {
    const elements = {
        boxInvite:      document.getElementById("memoirSearchInvite"),
        triggerInvite:  document.getElementById("memoirSearchTriggerInvite"),
        inputInvite:    document.getElementById('memoirSearchInputInvite'),
        dropMenuInvite: document.getElementById('memoirSearchDropdownInvite'),
        dropBodyInvite: document.getElementById('memoirDropBodyInvite'),
        footerInvite:   document.getElementById('memoirDropFooterInvite'),
        seeAllLinkInvite:  document.getElementById('memoirSeeAllInvite'),
        queryLabelInvite:  document.getElementById('memoirQueryLabelInvite'),
    }

    if (!elements.boxInvite || !elements.inputInvite) {
        console.warn('[memoir-search] elements not found');
        return;
    }

    const memoryId = elements.boxInvite.dataset.memoryId;
    const bsDropdown = new bootstrap.Dropdown(elements.triggerInvite, { autoClose: true });
    let debounceTimer  = null;
    let abortController = null;

    const escHtml = (str) => String(str || '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');

    const initials = (first = '', last = '') =>
        (first.charAt(0) + last.charAt(0)).toUpperCase();

    const avatarHTML = ({ profileImage, firstname, lastname }) => {
        if (profileImage) {
            return `<div class="memoir-drop-avatar"><img src="${escHtml(profileImage)}" alt=""/></div>`;
        }
        return `<div class="memoir-drop-avatar">${escHtml(initials(firstname, lastname))}</div>`;
    };

    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';

    const createRow = (memoryId, user) => `
        <form method="post" action="/memories/${memoryId}/invite?userId=${user.id}" style="margin:0;">
            <input type="hidden" name="_csrf" value="${csrfToken}"/>
            <button type="submit" class="memoir-drop-row dropdown-item w-100 text-start border-0 bg-transparent">
                ${avatarHTML(user)}
                <div>
                    <div class="memoir-drop-name">${escHtml(`${user.firstname} ${user.lastname}`)}</div>
                    <div class="memoir-drop-sub">${escHtml(`@${user.username}`)}</div>
                </div>
            </button>
        </form>`;

    const renderResults = (data, query) => {
        const { users = [] } = data;

        if (!users.length) {
            elements.dropBodyInvite.innerHTML = `<span class="dropdown-item-text text-muted small">No results for &ldquo;${escHtml(query)}&rdquo;</span>`;
            elements.footerInvite.style.display = 'none';
            return;
        }

        const userHtml = users.slice(0, 3).map(u =>
            createRow(memoryId, u)
        ).join('');

        elements.dropBodyInvite.innerHTML =
            `<div class="memoir-drop-section-label">People</div>${userHtml}`;
        elements.queryLabelInvite.textContent = query;
        elements.seeAllLinkInvite.href = `/search?q=${encodeURIComponent(query)}`;
        elements.footerInvite.style.display = 'block';
    };

    elements.triggerInvite.addEventListener('click', () => {
        elements.inputInvite.focus();
        bsDropdown.show();
    });

    elements.inputInvite.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') { bsDropdown.hide(); elements.inputInvite.blur(); }
        if (e.key === 'Enter' && elements.inputInvite.value.trim()) {
            window.location.href = `/search?q=${encodeURIComponent(elements.inputInvite.value.trim())}`;
        }
    });

    elements.inputInvite.addEventListener('input', async () => {
        const q = elements.inputInvite.value.trim();

        if (!q) {
            elements.footerInvite.style.display = 'none';
            elements.dropBodyInvite.innerHTML =
                '<span class="dropdown-item-text text-muted small">Start typing to search\u2026</span>';
            return;
        }

        elements.queryLabelInvite.textContent = q;
        elements.footerInvite.style.display = 'block';

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async () => {
            if (abortController) abortController.abort();
            abortController = new AbortController();

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

    let suppressOpen = false;

    elements.inputInvite.addEventListener('focus', () => {
        if (!suppressOpen) bsDropdown.show();
    });

    document.addEventListener('click', (e) => {
        if (!elements.boxInvite.contains(e.target)) {
            suppressOpen = true;
            bsDropdown.hide();
            setTimeout(() => suppressOpen = false, 200);
        }
    });
})();