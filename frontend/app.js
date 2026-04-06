/**
 * app.js — Shared utilities used by every page.
 *
 * Functions:
 *   getUser()          — Read logged-in user from localStorage
 *   requireLogin(role) — Redirect to login if not authenticated (or wrong role)
 *   apiFetch(url, opt) — Wrapper around fetch() that always sends session cookies
 *   logout()           — Call /logout, clear localStorage, redirect to login
 *   renderNav()        — Populate nav links based on login state
 */

const API = 'http://localhost:8080';

// ── getUser ─────────────────────────────────────────────────────────────────
// Returns the user object stored in localStorage, or null if not logged in.
function getUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

// ── requireLogin ─────────────────────────────────────────────────────────────
// Call at the top of any protected page.
// role (optional): 'seeker' | 'employer' | 'admin'
function requireLogin(role) {
    const user = getUser();
    if (!user) {
        window.location.href = '/login.html';
        return null;
    }
    if (role && user.role !== role) {
        alert('Access denied. This page is for ' + role + 's only.');
        window.location.href = '/index.html';
        return null;
    }
    return user;
}

// ── apiFetch ─────────────────────────────────────────────────────────────────
// Wrapper around fetch() that:
//   - prepends the API base URL
//   - always includes credentials (session cookies)
//   - sets Content-Type to JSON for POST/PUT bodies
//   - injects X-User-Id and X-User-Role headers if logged in
async function apiFetch(path, options = {}) {
    const user = getUser();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (user) {
        headers['X-User-Id'] = user.id;
        headers['X-User-Role'] = user.role;
    }

    const defaults = {
        credentials: 'include',
        headers: headers,
    };
    const res = await fetch(API + path, { ...defaults, ...options, headers });
    return res;
}

// ── logout ────────────────────────────────────────────────────────────────────
async function logout() {
    await apiFetch('/logout', { method: 'POST' });
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}

// ── renderNav ─────────────────────────────────────────────────────────────────
// Call with the id of the element to inject nav links into.
// Renders different links depending on role.
function renderNav(containerId = 'nav-links') {
    const el   = document.getElementById(containerId);
    const user = getUser();
    if (!el) return;

    if (!user) {
        el.innerHTML = `
            <a href="/index.html">Home</a>
            <a href="/jobs.html">Browse Jobs</a>
            <a href="/login.html">Login</a>
            <a href="/register.html" class="btn-primary">Register</a>
        `;
        return;
    }

    let dashLink = '';
    if (user.role === 'seeker')   dashLink = `<a href="/seeker-dashboard.html">Dashboard</a>`;
    if (user.role === 'employer') dashLink = `<a href="/employer-dashboard.html">Dashboard</a>`;
    if (user.role === 'admin')    dashLink = `<a href="/admin-dashboard.html">Admin</a>`;

    el.innerHTML = `
        <a href="/index.html">Home</a>
        <a href="/jobs.html">Browse Jobs</a>
        ${dashLink}
        <span style="color:var(--muted);padding: 0 8px;font-size:0.88rem;">Hi, ${user.name}</span>
        <button onclick="logout()" class="btn-outline">Logout</button>
    `;
}

// ── badgeClass ────────────────────────────────────────────────────────────────
// Returns the CSS class for a status badge.
function badgeClass(status) {
    const map = { Applied: 'badge-applied', Shortlisted: 'badge-shortlisted', Rejected: 'badge-rejected' };
    return 'badge ' + (map[status] || 'badge-applied');
}

// ── formatDate ────────────────────────────────────────────────────────────────
function formatDate(d) {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}
