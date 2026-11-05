// Ponto de entrada: liga os módulos entre si.
import { initChat } from './chat.js';
import { initTransactions } from './transactions.js';
import { initSheet } from './sheet.js';

let transactions;
const chat = initChat({ onDataChanged: () => transactions.load() });
transactions = initTransactions({ onError: (message) => chat.addMessage(message, 'bot') });

initSheet();
transactions.load().catch(() => {});
