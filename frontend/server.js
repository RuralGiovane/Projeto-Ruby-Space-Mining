import http from 'node:http';
import https from 'node:https';
import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const assets = new Map([
  ['/', ['index.html', 'text/html; charset=utf-8']],
  ['/app.js', ['app.js', 'text/javascript; charset=utf-8']],
  ['/styles.css', ['styles.css', 'text/css; charset=utf-8']],
]);

export function createServer({
  commandUrl = process.env.COMMAND_SERVICE_URL || 'http://localhost:8080',
  miningUrl = process.env.MINING_SERVICE_URL || 'http://localhost:8082',
} = {}) {
  return http.createServer(async (request, response) => {
    const pathname = new URL(request.url, 'http://localhost').pathname;
    const route = pathname === '/api/command'
      ? { method: 'POST', target: new URL('/command', commandUrl) }
      : pathname === '/api/counts'
        ? { method: 'GET', target: new URL('/commands/counts', miningUrl) }
        : null;

    response.setHeader('X-Content-Type-Options', 'nosniff');
    response.setHeader('Cache-Control', 'no-store');
    if (route) {
      if (request.method !== route.method) {
        response.writeHead(405, { Allow: route.method }).end();
        return;
      }
      const transport = route.target.protocol === 'https:' ? https : http;
      const headers = { accept: 'application/json' };
      if (request.headers['content-type']) headers['content-type'] = request.headers['content-type'];
      const upstream = transport.request(route.target, { method: route.method, headers }, result => {
        response.writeHead(result.statusCode, {
          'Content-Type': result.headers['content-type'] || 'application/json',
        });
        result.on('error', () => response.destroy());
        result.pipe(response);
      });
      upstream.setTimeout(35000, () => upstream.destroy(new Error('Timeout')));
      upstream.on('error', () => {
        if (response.headersSent) return response.destroy();
        response.writeHead(503, { 'Content-Type': 'application/json' });
        response.end(JSON.stringify({ error: 'Não foi possível conectar ao serviço. Verifique se as aplicações estão em execução.' }));
      });
      request.on('aborted', () => upstream.destroy());
      response.on('close', () => { if (!response.writableFinished) upstream.destroy(); });
      request.pipe(upstream);
      return;
    }

    const asset = assets.get(pathname);
    if (!asset) return response.writeHead(404).end('Não encontrado');
    if (!['GET', 'HEAD'].includes(request.method)) return response.writeHead(405, { Allow: 'GET, HEAD' }).end();
    try {
      const content = await readFile(new URL(`./public/${asset[0]}`, import.meta.url));
      response.writeHead(200, { 'Content-Type': asset[1] });
      response.end(request.method === 'HEAD' ? undefined : content);
    } catch {
      response.writeHead(500).end('Não foi possível carregar a página.');
    }
  });
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const port = Number(process.env.PORT || 3000);
  createServer().listen(port, '0.0.0.0', () => {
    console.log(`Space Mining: http://localhost:${port}`);
  });
}
