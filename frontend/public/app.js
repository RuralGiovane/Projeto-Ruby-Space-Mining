const commands = ['RIGHT', 'LEFT', 'FRONT', 'BACK', 'OPEN', 'CLOSE'];
const commandButtons = [...document.querySelectorAll('[data-command]')];
const sendButton = document.querySelector('#send-command');
const sendLabel = document.querySelector('#send-label');
const feedback = document.querySelector('#send-feedback');
const refreshButton = document.querySelector('#refresh-counts');
const countsStatus = document.querySelector('#counts-status');
const countsError = document.querySelector('#counts-error');
const numberFormat = new Intl.NumberFormat('pt-BR');
const timeFormat = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
let selected = null;
let sending = false;
let refreshing = false;
let hasCounts = false;

for (const button of commandButtons) {
  button.addEventListener('click', () => {
    if (sending) return;
    selected = button.dataset.command;
    for (const item of commandButtons) item.setAttribute('aria-pressed', String(item === button));
    document.querySelector('#selected-command').textContent = selected;
    sendLabel.textContent = `Enviar ${selected}`;
    sendButton.disabled = false;
  });
}

async function api(path, options = {}) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), options.method === 'POST' ? 40000 : 10000);
  try {
    const response = await fetch(path, { ...options, signal: controller.signal, cache: 'no-store' });
    const text = await response.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch { data = null; }
    if (!response.ok) throw new Error(data?.error || `O serviço retornou um erro (${response.status}). Tente novamente mais tarde.`);
    return data;
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('O serviço demorou para responder. O resultado do envio pode ser incerto; confira as contagens antes de reenviar.');
    if (error instanceof TypeError) throw new Error('Conexão interrompida. Verifique os serviços e as contagens antes de reenviar.');
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

function addHistory(command, success, message) {
  document.querySelector('#history-empty').hidden = true;
  const list = document.querySelector('#history-list');
  const row = document.createElement('li');
  const time = document.createElement('time');
  const now = new Date();
  time.dateTime = now.toISOString();
  time.textContent = timeFormat.format(now);
  time.className = 'history-time';
  const label = document.createElement('span');
  label.textContent = command;
  label.className = 'history-command';
  const badge = document.createElement('span');
  badge.textContent = success ? 'Na fila' : 'Sem confirmação';
  badge.className = `history-badge${success ? '' : ' error'}`;
  row.title = message;
  row.append(time, label, badge);
  list.prepend(row);
  while (list.children.length > 20) list.lastElementChild.remove();
}

sendButton.addEventListener('click', async () => {
  if (!selected || sending) return;
  const command = selected;
  sending = true;
  sendButton.disabled = true;
  sendButton.setAttribute('aria-busy', 'true');
  commandButtons.forEach(button => { button.disabled = true; });
  sendLabel.textContent = 'Validando e enviando…';
  feedback.className = 'feedback';
  feedback.textContent = 'A validação pode realizar novas tentativas. Aguarde alguns segundos.';
  try {
    await api('/api/command', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ command }) });
    const message = `${command} enviado para a fila. A contagem será atualizada após o processamento.`;
    feedback.textContent = message;
    feedback.className = 'feedback success';
    addHistory(command, true, message);
    await refreshCounts();
  } catch (error) {
    feedback.textContent = error.message;
    feedback.className = 'feedback error';
    addHistory(command, false, error.message);
  } finally {
    sending = false;
    commandButtons.forEach(button => { button.disabled = false; });
    sendButton.disabled = false;
    sendButton.setAttribute('aria-busy', 'false');
    sendLabel.textContent = `Enviar ${selected}`;
  }
});

async function refreshCounts() {
  if (refreshing) return;
  refreshing = true;
  refreshButton.disabled = true;
  try {
    const counts = await api('/api/counts');
    if (!counts || commands.some(command => !Number.isSafeInteger(counts[command]) || counts[command] < 0)) {
      throw new Error('O serviço retornou contagens em um formato inesperado.');
    }
    let total = 0;
    for (const command of commands) {
      document.querySelector(`[data-count="${command}"]`).textContent = numberFormat.format(counts[command]);
      total += counts[command];
    }
    document.querySelector('#total-count').textContent = numberFormat.format(total);
    document.querySelector('#last-updated').textContent = `Atualizado às ${timeFormat.format(new Date())}`;
    countsStatus.textContent = 'Contagens online';
    countsStatus.className = 'connection-status online';
    countsError.hidden = true;
    hasCounts = true;
  } catch (error) {
    countsStatus.textContent = 'Sem atualização';
    countsStatus.className = 'connection-status offline';
    countsError.textContent = hasCounts ? 'Exibindo a última leitura. Não foi possível atualizar as contagens.' : 'Não foi possível carregar as contagens. Verifique se o MiningService está em execução.';
    countsError.hidden = false;
  } finally {
    refreshing = false;
    refreshButton.disabled = false;
  }
}

refreshButton.addEventListener('click', refreshCounts);
document.addEventListener('visibilitychange', () => { if (!document.hidden) refreshCounts(); });
setInterval(() => { if (!document.hidden) refreshCounts(); }, 3000);
refreshCounts();
