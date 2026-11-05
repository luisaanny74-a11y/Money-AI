// Gaveta de lançamentos (abre pelo menu ⋮).
import { $ } from './ui.js';

export function initSheet() {
  const toggle = (open) => {
    $('sheet').classList.toggle('open', open);
    $('sheet').setAttribute('aria-hidden', String(!open));
    $('backdrop').hidden = !open;
  };
  $('openSheet').onclick = () => toggle(true);
  $('closeSheet').onclick = () => toggle(false);
  $('backdrop').onclick = () => toggle(false);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') toggle(false); });
}
