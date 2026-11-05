// Utilitários e ícones compartilhados.
export const $ = (id) => document.getElementById(id);
export const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

const ICONS = {
  budget:  '<path d="M3 7h16a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7zm0 0l2-3h12"/><circle cx="17" cy="13" r="1"/>',
  balance: '<path d="M4 20V10m6 10V4m6 16v-7m4 7H2"/>',
  spend:   '<path d="M21 12a9 9 0 11-9-9v9h9z"/>',
  tips:    '<path d="M9 18h6m-5 3h4M12 3a6 6 0 00-4 10.5c.6.6 1 1.4 1 2.5h6c0-1.1.4-1.9 1-2.5A6 6 0 0012 3z"/>',
  expense: '<path d="M12 5v14m-6-6l6 6 6-6"/>',
  income:  '<path d="M12 19V5m-6 6l6-6 6 6"/>',
  invest:  '<path d="M3 17l6-6 4 4 8-8m0 0h-5m5 0v5"/>',
  doc:     '<path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8l-5-5zm0 0v5h5M9 13h6m-6 4h6"/>',
};

export const icon = (name) =>
  `<svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${ICONS[name]}</svg>`;

export const SPARK = '<svg class="spark" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1c.6 5.7 5.3 10.4 11 11-5.7.6-10.4 5.3-11 11-.6-5.7-5.3-10.4-11-11 5.7-.6 10.4-5.3 11-11z"/></svg>';
export const SEND  = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 19V5m-6 6l6-6 6 6"/></svg>';
export const MIC   = '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="9" y="3" width="6" height="12" rx="3"/><path d="M5 11a7 7 0 0014 0m-7 7v3"/></svg>';
