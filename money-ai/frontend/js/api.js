// Toda comunicação com o backend fica aqui.
import { API_BASE } from './config.js';

async function request(path, options = {}) {
  const res = await fetch(API_BASE + path, options);
  if (!res.ok) {
    const problem = await res.json().catch(() => ({}));
    throw new Error(problem.detail || 'Algo deu errado. Tente de novo.');
  }
  return res.status === 204 ? null : res.json();
}

const withJson = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});

export const chatApi = {
  send: (message) => request('/api/chat', withJson('POST', { message })),
  upload: (file) => {
    const form = new FormData();
    form.append('file', file);
    return request('/api/chat/upload', { method: 'POST', body: form });
  },
};

export const transactionsApi = {
  list: () => request('/api/transactions'),
  create: (data) => request('/api/transactions', withJson('POST', data)),
  update: (id, data) => request(`/api/transactions/${id}`, withJson('PUT', data)),
  remove: (id) => request(`/api/transactions/${id}`, { method: 'DELETE' }),
};
