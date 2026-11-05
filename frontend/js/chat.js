// Conversa: mensagens, atalhos, sugestões, anexos e voz.
import { chatApi } from './api.js';
import { $, icon, SPARK, SEND, MIC } from './ui.js';

const SHORTCUTS = [
  { label: 'Criar orçamento',     icon: 'budget',  send: 'Criar orçamento' },
  { label: 'Ver meu saldo',       icon: 'balance', send: 'Ver meu saldo' },
  { label: 'Analisar gastos',     icon: 'spend',   send: 'Analisar gastos' },
  { label: 'Dicas de economia',   icon: 'tips',    send: 'Dicas de economia' },
  { label: 'Registrar gasto',     icon: 'expense', prefill: 'gastei ' },
  { label: 'Registrar receita',   icon: 'income',  prefill: 'recebi ' },
  { label: 'Aprender a investir', icon: 'invest',  send: 'Aprender a investir' },
  { label: 'Enviar documento',    icon: 'doc',     pickFile: true },
];

const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;

/** @param {{onDataChanged: () => void}} deps */
export function initChat({ onDataChanged }) {
  function addMessage(text, who) {
    $('stage').classList.add('chatting');
    const bubble = document.createElement('div');
    bubble.className = `msg ${who}`;
    bubble.textContent = text;

    let node = bubble;
    if (who === 'bot') {
      node = document.createElement('div');
      node.className = 'bot-row';
      node.innerHTML = SPARK;
      node.append(bubble);
    }
    $('messages').append(node);
    $('stage').scrollTop = $('stage').scrollHeight;
  }

  function showFollowups(suggestions = []) {
    $('followups').hidden = suggestions.length === 0;
    if (!suggestions.length) return;
    $('ctaMain').textContent = suggestions[0];
    $('ctaMain').onclick = () => sendMessage(suggestions[0]);
  }

  async function handle(promise) {
    showFollowups([]);
    try {
      const { reply, dataChanged, suggestions } = await promise;
      addMessage(reply, 'bot');
      showFollowups(suggestions);
      if (dataChanged) onDataChanged();
    } catch (e) {
      addMessage(e.message === 'Failed to fetch' ? 'Não consegui falar com o servidor. Verifique se o backend está rodando.' : e.message, 'bot');
    }
  }

  function sendMessage(message) {
    addMessage(message, 'user');
    handle(chatApi.send(message));
  }

  /* ----- botão branco: microfone quando vazio, enviar quando há texto ----- */
  function updateSendButton() {
    const showSend = $('text').value.trim().length > 0 || !Recognition;
    $('sendBtn').innerHTML = showSend ? SEND : MIC;
    $('sendBtn').setAttribute('aria-label', showSend ? 'Enviar' : 'Falar');
  }

  function startVoice() {
    if (!Recognition) return;
    const rec = new Recognition();
    rec.lang = 'pt-BR';
    rec.onstart = () => $('sendBtn').classList.add('listening');
    rec.onend = () => { $('sendBtn').classList.remove('listening'); updateSendButton(); };
    rec.onresult = (ev) => { $('text').value = ev.results[0][0].transcript; };
    rec.start();
  }

  /* ----- atalhos ----- */
  SHORTCUTS.forEach((s) => {
    const btn = document.createElement('button');
    btn.className = 'chip';
    btn.type = 'button';
    btn.innerHTML = `<i>${icon(s.icon)}</i><span></span>`;
    btn.querySelector('span').textContent = s.label;
    btn.onclick = () => {
      if (s.send) return sendMessage(s.send);
      if (s.prefill) { $('text').value = s.prefill; $('text').focus(); return updateSendButton(); }
      if (s.pickFile) $('file').click();
    };
    $('chips').append(btn);
  });

  /* ----- eventos ----- */
  $('chatForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const message = $('text').value.trim();
    if (!message) return startVoice();
    $('text').value = '';
    updateSendButton();
    sendMessage(message);
  });

  $('file').addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;
    addMessage(`📎 ${file.name}`, 'user');
    handle(chatApi.upload(file));
    e.target.value = '';
  });

  $('text').addEventListener('input', updateSendButton);
  $('ctaSkip').onclick = () => showFollowups([]);
  $('newChat').onclick = () => {
    $('messages').replaceChildren();
    $('stage').classList.remove('chatting');
    showFollowups([]);
  };

  updateSendButton();
  return { addMessage };
}
