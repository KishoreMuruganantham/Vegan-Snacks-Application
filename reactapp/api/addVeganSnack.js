/**
 * `POST /api/addVeganSnack`
 *
 * Stores a new catalogue entry. The payload is validated with exactly the same
 * rules and messages as the Spring Boot API, a name that is already taken is
 * reported as a conflict and the stored entry is returned with its derived
 * pricing, freshness and valuation figures.
 */

const { jsonHeaders, nameExists, seed, store, toResponse, validate } = require('./_catalogue');

/**
 * Reads the request body and parses it as JSON.
 *
 * The hosting platform parses a JSON payload and exposes it on the request
 * object. Reading that property throws when the payload is malformed, so the
 * access is guarded and the raw stream is used as a fallback.
 *
 * @param {object} request the request
 * @returns {Promise<object>} the parsed body, or `null` when it is unusable
 */
const readBody = async (request) => {
  let parsed;

  try {
    parsed = request.body;
  } catch (parseError) {
    // The platform could not parse the payload, the stream is read below.
    parsed = undefined;
  }

  if (parsed !== undefined && parsed !== null) {
    if (typeof parsed === 'object') {
      return parsed;
    }
    if (typeof parsed === 'string') {
      if (!parsed.trim()) {
        return null;
      }
      try {
        return JSON.parse(parsed);
      } catch (parseError) {
        return null;
      }
    }
  }

  const chunks = [];
  for await (const chunk of request) {
    chunks.push(chunk);
  }
  const raw = Buffer.concat(chunks).toString('utf8').replace(/^\uFEFF/, '');
  if (!raw.trim()) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (parseError) {
    return null;
  }
};

module.exports = async function handler(request, response) {
  if (request.method === 'OPTIONS') {
    response.writeHead(204, jsonHeaders());
    response.end();
    return;
  }

  if (request.method !== 'POST') {
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

  const payload = await readBody(request);

  if (payload === null) {
    response.writeHead(400, jsonHeaders());
    response.end(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        status: 400,
        error: 'Bad Request',
        message: 'Request body is missing or malformed.',
        fieldErrors: {},
      })
    );
    return;
  }

  const fieldErrors = validate(payload);
  if (Object.keys(fieldErrors).length > 0) {
    response.writeHead(400, jsonHeaders());
    response.end(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        status: 400,
        error: 'Bad Request',
        message: 'Snack details are invalid.',
        fieldErrors,
      })
    );
    return;
  }

  seed();

  const snackName = String(payload.snackName).trim().replace(/\s+/g, ' ');
  if (nameExists(snackName)) {
    response.writeHead(409, jsonHeaders());
    response.end(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        status: 409,
        error: 'Conflict',
        message: 'A snack with this name already exists.',
        fieldErrors: { snackName: 'A snack with this name already exists' },
      })
    );
    return;
  }

  const record = store(payload);
  response.writeHead(200, jsonHeaders());
  response.end(JSON.stringify(toResponse(record)));
};
