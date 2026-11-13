/**
 * Runs the serverless functions locally with a mocked request so that the
 * behaviour can be verified without a deployment.
 *
 * Usage: node scripts/test-api.js
 */

const path = require('path');

const API_DIR = path.join(__dirname, '..', 'api');

/** Builds a mock request and collects the response. */
const run = async (modulePath, { method = 'GET', body = null, headers = {} } = {}) => {
  const handler = require(path.join(API_DIR, modulePath));

  const request = {
    method,
    headers,
    body,
    url: '/',
    async *[Symbol.asyncIterator]() {
      if (body !== null && typeof body === 'string') {
        yield Buffer.from(body);
      }
    },
  };

  let statusCode = 0;
  let payload = '';
  const response = {
    writeHead(status, responseHeaders) {
      statusCode = status;
      Object.assign(headers, responseHeaders || {});
      return response;
    },
    end(chunk) {
      if (chunk) payload += chunk;
      return response;
    },
    getHeader(name) {
      return headers[name];
    },
  };

  await handler(request, response);
  return { statusCode, payload };
};

const show = (label, result) => {
  console.log(`\n${label} -> HTTP ${result.statusCode}`);
  console.log(result.payload.length > 600 ? `${result.payload.slice(0, 600)}...` : result.payload);
};

(async () => {
  show('GET /api/getAllVeganSnacks', await run('getAllVeganSnacks.js'));

  show(
    'POST /api/addVeganSnack (valid, parsed body)',
    await run('addVeganSnack.js', {
      method: 'POST',
      body: {
        snackName: 'Hosted Test',
        snackType: 'Fruit Snacks',
        quantity: 11,
        price: 42.5,
        expiryInMonths: 3,
      },
    })
  );

  show(
    'POST /api/addVeganSnack (valid, raw string body)',
    await run('addVeganSnack.js', {
      method: 'POST',
      body: JSON.stringify({
        snackName: 'Hosted Test Two',
        snackType: 'Chocolate',
        quantity: 4,
        price: 9.99,
        expiryInMonths: 2,
      }),
    })
  );

  show(
    'POST /api/addVeganSnack (invalid)',
    await run('addVeganSnack.js', {
      method: 'POST',
      body: { snackName: '', snackType: '', quantity: 0, price: -5, expiryInMonths: -1 },
    })
  );

  show(
    'POST /api/addVeganSnack (duplicate)',
    await run('addVeganSnack.js', {
      method: 'POST',
      body: { snackName: 'Hosted Test', snackType: 'Fruit Snacks', quantity: 1, price: 5, expiryInMonths: 1 },
    })
  );

  show('GET /api/v1/health', await run('v1/health.js'));
  show('GET /api/v1/snacks/stats', await run('v1/snacks/stats.js'));
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
