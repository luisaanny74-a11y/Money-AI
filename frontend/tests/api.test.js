import { test, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { chatApi, transactionsApi } from '../js/api.js';
import { API_BASE } from '../js/config.js';

let calls;

/** Substitui o fetch por uma resposta fixa e guarda as chamadas feitas. */
function mockFetch(status, body) {
  globalThis.fetch = async (url, options = {}) => {
    calls.push({ url, options });
    return new Response(status === 204 ? null : JSON.stringify(body), { status });
  };
}

beforeEach(() => { calls = []; });

test('chat.send faz POST em /api/chat com a mensagem em JSON', async () => {
  mockFetch(200, { reply: 'oi', dataChanged: false, suggestions: [] });

  const data = await chatApi.send('Ver meu saldo');

  assert.equal(calls[0].url, `${API_BASE}/api/chat`);
  assert.equal(calls[0].options.method, 'POST');
  assert.equal(calls[0].options.headers['Content-Type'], 'application/json');
  assert.deepEqual(JSON.parse(calls[0].options.body), { message: 'Ver meu saldo' });
  assert.equal(data.reply, 'oi');
});

test('chat.upload envia o arquivo como multipart/form-data', async () => {
  mockFetch(200, { reply: 'li o arquivo', dataChanged: false, suggestions: [] });
  const file = new File(['conteudo'], 'extrato.pdf', { type: 'application/pdf' });

  await chatApi.upload(file);

  assert.equal(calls[0].url, `${API_BASE}/api/chat/upload`);
  assert.equal(calls[0].options.method, 'POST');
  assert.ok(calls[0].options.body instanceof FormData);
  assert.equal(calls[0].options.body.get('file').name, 'extrato.pdf');
  assert.equal(calls[0].options.headers, undefined, 'o navegador define o Content-Type do multipart');
});

test('transactions.list faz GET em /api/transactions', async () => {
  mockFetch(200, [{ id: 1 }]);

  const items = await transactionsApi.list();

  assert.equal(calls[0].url, `${API_BASE}/api/transactions`);
  assert.deepEqual(items, [{ id: 1 }]);
});

test('transactions.create faz POST com o corpo em JSON', async () => {
  mockFetch(201, { id: 7 });
  const payload = { description: 'Mercado', amount: 50, type: 'EXPENSE', category: '', date: null };

  await transactionsApi.create(payload);

  assert.equal(calls[0].options.method, 'POST');
  assert.deepEqual(JSON.parse(calls[0].options.body), payload);
});

test('transactions.update faz PUT em /api/transactions/{id}', async () => {
  mockFetch(200, { id: 3 });

  await transactionsApi.update(3, { description: 'Novo' });

  assert.equal(calls[0].url, `${API_BASE}/api/transactions/3`);
  assert.equal(calls[0].options.method, 'PUT');
});

test('transactions.remove faz DELETE e aceita resposta 204 sem corpo', async () => {
  mockFetch(204);

  const result = await transactionsApi.remove(3);

  assert.equal(calls[0].url, `${API_BASE}/api/transactions/3`);
  assert.equal(calls[0].options.method, 'DELETE');
  assert.equal(result, null);
});

test('erro da API usa a mensagem "detail" devolvida pelo backend', async () => {
  mockFetch(404, { detail: 'Lançamento 9 não encontrado.' });

  await assert.rejects(() => transactionsApi.list(), { message: 'Lançamento 9 não encontrado.' });
});

test('erro sem JSON usa uma mensagem genérica', async () => {
  globalThis.fetch = async () => new Response('<html>erro</html>', { status: 500 });

  await assert.rejects(() => chatApi.send('oi'), { message: 'Algo deu errado. Tente de novo.' });
});
