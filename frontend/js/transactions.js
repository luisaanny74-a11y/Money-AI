// Lançamentos: listagem e CRUD (criar, editar, excluir).
import { transactionsApi } from './api.js';
import { $, brl } from './ui.js';

/** @param {{onError: (message: string) => void}} deps */
export function initTransactions({ onError }) {
  async function load() {
    const items = await transactionsApi.list();
    const list = $('list');
    list.replaceChildren();
    if (!items.length) list.innerHTML = '<li class="empty">Nenhum lançamento. Adicione acima ou fale com o Money-AI.</li>';

    let balance = 0;
    items.forEach((t) => {
      balance += t.type === 'INCOME' ? t.amount : -t.amount;
      list.append(renderItem(t));
    });
    $('balance').textContent = brl.format(balance);
  }

  function renderItem(t) {
    const li = document.createElement('li');
    li.className = 'item';
    li.innerHTML = `
      <div><div class="d"></div><small></small></div>
      <div class="amount ${t.type}">${t.type === 'INCOME' ? '+' : '−'} ${brl.format(t.amount)}</div>
      <div class="actions"><button data-edit>Editar</button><button data-del>Excluir</button></div>`;
    li.querySelector('.d').textContent = t.description;
    li.querySelector('small').textContent = `${t.category} · ${new Date(t.date + 'T00:00').toLocaleDateString('pt-BR')}`;
    li.querySelector('[data-edit]').onclick = () => startEdit(t);
    li.querySelector('[data-del]').onclick = async () => { await transactionsApi.remove(t.id); load(); };
    return li;
  }

  function startEdit(t) {
    $('txId').value = t.id;
    $('txDesc').value = t.description;
    $('txAmount').value = t.amount;
    $('txType').value = t.type;
    $('txCategory').value = t.category;
    $('txDate').value = t.date;
    $('txSave').textContent = 'Salvar alterações';
    $('txCancel').hidden = false;
  }

  function resetForm() {
    $('txForm').reset();
    $('txId').value = '';
    $('txSave').textContent = 'Adicionar';
    $('txCancel').hidden = true;
  }

  $('txCancel').onclick = resetForm;
  $('txForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = $('txId').value;
    const data = {
      description: $('txDesc').value,
      amount: Number($('txAmount').value),
      type: $('txType').value,
      category: $('txCategory').value,
      date: $('txDate').value || null,
    };
    try {
      await (id ? transactionsApi.update(id, data) : transactionsApi.create(data));
      resetForm();
      load();
    } catch (err) { onError(err.message); }
  });

  return { load };
}
