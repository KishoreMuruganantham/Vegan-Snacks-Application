#!/usr/bin/env node
/**
 * Cross-platform launcher for the Create React App scripts.
 *
 * The Create React App 5 toolchain still drives Webpack 5, which asks
 * OpenSSL for an MD4 hash when it fingerprints modules. MD4 was removed from
 * OpenSSL 3's default provider set, so the hash call fails on Node 17 and
 * newer with `error:0308010C:digital envelope routines::unsupported`.
 *
 * Rather than pinning the project to a single Node major version, this
 * launcher re-executes the requested Create React App command with the
 * OpenSSL legacy provider enabled whenever the running Node version needs it.
 * On Node 16 (the version referenced by the problem statement) the command is
 * launched untouched, so the behaviour is identical to calling the script
 * directly.
 *
 * The Create React App entry point is started through the current Node binary
 * instead of the generated `react-scripts.cmd` shim. That keeps the launcher
 * free of a shell, which in turn keeps it correct on Windows paths that
 * contain spaces and free of the argument escaping warnings that a shell
 * based launcher produces.
 *
 * Usage: node scripts/cra.js <start|build|test> [extra arguments...]
 */

'use strict';

const { spawn } = require('child_process');
const path = require('path');

const LEGACY_PROVIDER_FLAG = '--openssl-legacy-provider';
const COMMANDS = ['start', 'build', 'test'];

const command = process.argv[2];
const forwardedArgs = process.argv.slice(3);

if (!COMMANDS.includes(command)) {
  console.error(
    `Usage: node scripts/cra.js <${COMMANDS.join('|')}> [options]\n` +
      '       For example: node scripts/cra.js start'
  );
  process.exit(1);
}

/**
 * Decides whether the legacy OpenSSL provider has to be enabled.
 *
 * @returns {boolean} true when the flag must be added to NODE_OPTIONS.
 */
function requiresLegacyProvider() {
  if (process.env.CRA_SKIP_OPENSSL_WORKAROUND === 'true') {
    return false;
  }
  const major = Number.parseInt(process.versions.node.split('.')[0], 10);
  return Number.isFinite(major) && major >= 17;
}

/**
 * Builds the environment for the child process.
 *
 * @returns {NodeJS.ProcessEnv} the environment to hand to the child.
 */
function buildEnvironment() {
  const env = Object.assign({}, process.env);
  const currentOptions = env.NODE_OPTIONS || '';

  if (requiresLegacyProvider() && !currentOptions.includes(LEGACY_PROVIDER_FLAG)) {
    env.NODE_OPTIONS = currentOptions
      ? `${currentOptions} ${LEGACY_PROVIDER_FLAG}`
      : LEGACY_PROVIDER_FLAG;
  }
  return env;
}

/** Absolute path of the Create React App entry point. */
const entryPoint = path.join(
  __dirname,
  '..',
  'node_modules',
  'react-scripts',
  'bin',
  'react-scripts.js'
);

const child = spawn(process.execPath, [entryPoint, command, ...forwardedArgs], {
  stdio: 'inherit',
  env: buildEnvironment(),
});

child.on('error', (error) => {
  console.error(`Failed to start "react-scripts ${command}": ${error.message}`);
  process.exit(1);
});

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code === null ? 1 : code);
});

// Forward the signals that stop a development server so the child receives
// them as well and shuts down cleanly.
['SIGINT', 'SIGTERM'].forEach((signal) => {
  process.on(signal, () => {
    if (!child.killed) {
      child.kill(signal);
    }
  });
});
