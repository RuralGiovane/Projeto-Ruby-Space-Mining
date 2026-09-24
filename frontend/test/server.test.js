import { test } from 'node:test';
import assert from 'node:assert/strict';
import http from 'node:http';
import { createServer } from '../server.js';

async function listen(server, t) {
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  t.after(() => new Promise(resolve => { server.close(resolve); server.closeAllConnections(); }));
  return `http://127.0.0.1:${server.address().port}`;
}

test('serve a interface e seus arquivos estáticos', async t => {
  const base = await listen(createServer(), t);
  for (const [path, contentType] of [['/', 'text/html'], ['/app.js', 'text/javascript'], ['/styles.css', 'text/css']]) {
    const response = await fetch(base + path);
    assert.equal(response.status, 200);
    assert.ok(response.headers.get('content-type').startsWith(contentType));
    assert.ok((await response.text()).length > 100);
  }
  assert.equal((await fetch(base + '/server.js')).status, 404);
});

test('encaminha o JSON do comando e preserva o aceite 202', async t => {
  let received;
  const backend = await listen(http.createServer(async (request, response) => {
    let body = '';
    for await (const chunk of request) body += chunk;
    received = { path: request.url, method: request.method, type: request.headers['content-type'], body: JSON.parse(body) };
    response.writeHead(202).end();
  }), t);
  const base = await listen(createServer({ commandUrl: backend }), t);
  const response = await fetch(base + '/api/command', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ command: 'LEFT' }),
  });
  assert.equal(response.status, 202);
  assert.equal(await response.text(), '');
  assert.deepEqual(received, { path: '/command', method: 'POST', type: 'application/json', body: { command: 'LEFT' } });
});

test('consulta as contagens no MiningService', async t => {
  const counts = { RIGHT: 1, LEFT: 2, FRONT: 3, BACK: 4, OPEN: 5, CLOSE: 6 };
  let path;
  const backend = await listen(http.createServer((request, response) => {
    path = request.url;
    response.writeHead(200, { 'Content-Type': 'application/json' }).end(JSON.stringify(counts));
  }), t);
  const base = await listen(createServer({ miningUrl: backend }), t);
  assert.deepEqual(await (await fetch(base + '/api/counts')).json(), counts);
  assert.equal(path, '/commands/counts');
});

test('preserva erros de validação e indisponibilidade', async t => {
  for (const status of [400, 503]) {
    const backend = await listen(http.createServer((request, response) => {
      response.writeHead(status, { 'Content-Type': 'application/json' }).end(JSON.stringify({ error: 'Falha de teste' }));
    }), t);
    const base = await listen(createServer({ commandUrl: backend }), t);
    const response = await fetch(base + '/api/command', { method: 'POST', body: '{}' });
    assert.equal(response.status, status);
    assert.deepEqual(await response.json(), { error: 'Falha de teste' });
  }
});

test('retorna 503 quando não é possível conectar ao serviço', async t => {
  const closed = http.createServer();
  const backend = await listen(closed, t);
  await new Promise(resolve => closed.close(resolve));
  const base = await listen(createServer({ miningUrl: backend }), t);
  const response = await fetch(base + '/api/counts');
  assert.equal(response.status, 503);
  assert.match((await response.json()).error, /conectar ao serviço/);
});

test('rejeita métodos não suportados nas rotas da API', async t => {
  const base = await listen(createServer(), t);
  const response = await fetch(base + '/api/command');
  assert.equal(response.status, 405);
  assert.equal(response.headers.get('allow'), 'POST');
  assert.equal((await fetch(base + '/api/counts', { method: 'POST' })).status, 405);
});
