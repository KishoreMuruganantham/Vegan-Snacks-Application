/**
 * `GET /api/v1/health`
 *
 * Liveness probe of the deployment.
 */

const { catalogueSnapshot, jsonHeaders, seed } = require('../_catalogue');

module.exports = async function handler(request, response) {
  if (request.method === 'OPTIONS') {
    response.writeHead(204, jsonHeaders());
    response.end();
    return;
  }

  if (request.method !== 'GET') {
    response.writeHead(405, jsonHeaders());
    response.end(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        status: 405,
        error: 'Method Not Allowed',
        message: `The method ${request.method} is not supported by this endpoint.`,
        fieldErrors: {},
      })
    );
    return;
  }

  seed();

  response.writeHead(200, jsonHeaders());
  response.end(
    JSON.stringify({
      status: 'UP',
      service: 'vegan-snacks-api',
      snackCount: catalogueSnapshot().length,
    })
  );
};
