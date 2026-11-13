/**
 * `GET /api/getAllVeganSnacks`
 *
 * Returns every catalogue entry, newest first. The path mirrors the endpoint
 * of the Spring Boot API so that the client can address both deployments the
 * same way.
 */

const { catalogueSnapshot, jsonHeaders, seed, toResponse } = require('./_catalogue');

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

  const entries = catalogueSnapshot().map(toResponse);
  response.writeHead(200, jsonHeaders());
  response.end(JSON.stringify(entries));
};
