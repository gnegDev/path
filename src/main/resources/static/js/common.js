/**
 * VeraAI — Common JavaScript Utilities
 */

// ===========================
// Scroll Animations (Intersection Observer)
// ===========================
function initScrollAnimations() {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
        }
      });
    },
    { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
  );

  document.querySelectorAll('.fade-in').forEach(el => observer.observe(el));
}

// ===========================
// Animated Counter
// ===========================
function animateCounter(el, target, duration = 1500, suffix = '', prefix = '') {
  const isFloat = !Number.isInteger(target);
  const decimals = isFloat ? 1 : 0;
  const startTime = performance.now();

  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
    const value = eased * target;
    el.textContent = prefix + value.toFixed(decimals) + suffix;
    if (progress < 1) requestAnimationFrame(update);
  }

  requestAnimationFrame(update);
}

function initCounters() {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const el = entry.target;
          const target = parseFloat(el.dataset.target || '0');
          const suffix = el.dataset.suffix || '';
          const prefix = el.dataset.prefix || '';
          animateCounter(el, target, 1500, suffix, prefix);
          observer.unobserve(el);
        }
      });
    },
    { threshold: 0.5 }
  );

  document.querySelectorAll('[data-counter]').forEach(el => observer.observe(el));
}

// ===========================
// Sidebar Toggle
// ===========================
function initSidebar() {
  const sidebar = document.getElementById('sidebar');
  const mainContent = document.getElementById('main-content');
  const toggleBtn = document.getElementById('sidebar-toggle');

  if (!sidebar || !toggleBtn) return;

  toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('collapsed');
    if (mainContent) mainContent.classList.toggle('sidebar-collapsed');
    // Save state
    localStorage.setItem('sidebar-collapsed', sidebar.classList.contains('collapsed'));
  });

  // Restore state
  if (localStorage.getItem('sidebar-collapsed') === 'true') {
    sidebar.classList.add('collapsed');
    if (mainContent) mainContent.classList.add('sidebar-collapsed');
  }
}

// ===========================
// Tabs
// ===========================
function initTabs(containerSelector = '.tabs-nav', panelSelector = '.tab-panel') {
  const containers = document.querySelectorAll(containerSelector);
  containers.forEach(nav => {
    const buttons = nav.querySelectorAll('.tab-btn');
    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        // Deactivate all
        buttons.forEach(b => b.classList.remove('active'));
        const panels = document.querySelectorAll(panelSelector);
        panels.forEach(p => p.classList.remove('active'));
        // Activate clicked
        btn.classList.add('active');
        const target = document.getElementById(btn.dataset.tab);
        if (target) target.classList.add('active');
      });
    });
  });
}

// ===========================
// Modal
// ===========================
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('open');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('open');
}

function initModals() {
  // Close on overlay click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
      if (e.target === overlay) overlay.classList.remove('open');
    });
  });

  // Close buttons
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal-overlay');
      if (modal) modal.classList.remove('open');
    });
  });

  // Open buttons
  document.querySelectorAll('[data-open-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      openModal(btn.dataset.openModal);
    });
  });
}

// ===========================
// Toast notification
// ===========================
function showToast(message, type = 'info', duration = 3000) {
  let toast = document.getElementById('toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toast';
    toast.className = 'toast';
    document.body.appendChild(toast);
  }
  toast.textContent = message;
  toast.style.borderLeftColor = type === 'success' ? '#10B981' : type === 'error' ? '#EF4444' : '#00C9A7';
  toast.style.borderLeftWidth = '3px';
  toast.style.borderLeftStyle = 'solid';
  toast.classList.add('show');
  clearTimeout(toast._timeout);
  toast._timeout = setTimeout(() => toast.classList.remove('show'), duration);
}

// ===========================
// Role Toggle (Login page)
// ===========================
function initRoleToggle() {
  const btns = document.querySelectorAll('.role-btn');
  btns.forEach(btn => {
    btn.addEventListener('click', () => {
      btns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });
}

// ===========================
// Password toggle visibility
// ===========================
function initPasswordToggles() {
  document.querySelectorAll('.pw-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const inputId = btn.dataset.input;
      const input = document.getElementById(inputId);
      if (!input) return;
      if (input.type === 'password') {
        input.type = 'text';
        btn.querySelector('.icon-eye').style.display = 'none';
        btn.querySelector('.icon-eye-off').style.display = 'block';
      } else {
        input.type = 'password';
        btn.querySelector('.icon-eye').style.display = 'block';
        btn.querySelector('.icon-eye-off').style.display = 'none';
      }
    });
  });
}

// ===========================
// Auto-resize textarea
// ===========================
function initAutoResize() {
  document.querySelectorAll('textarea.auto-resize').forEach(ta => {
    ta.addEventListener('input', () => {
      ta.style.height = 'auto';
      ta.style.height = Math.min(ta.scrollHeight, 140) + 'px';
    });
  });
}

// ===========================
// Init all common features
// ===========================
document.addEventListener('DOMContentLoaded', () => {
  initScrollAnimations();
  initCounters();
  initSidebar();
  initTabs();
  initTabs('.tabs-bar');
  initModals();
  initRoleToggle();
  initPasswordToggles();
  initAutoResize();
});
