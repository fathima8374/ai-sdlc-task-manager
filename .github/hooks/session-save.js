'use strict';

const fs = require('node:fs');
const path = require('node:path');

const repositoryRoot = path.resolve(__dirname, '../..');
const sessionsDir = path.join(repositoryRoot, 'sessions');
const logPath = path.join(sessionsDir, '.hook-log.jsonl');
let input = '';

process.stdin.setEncoding('utf8');
process.stdin.on('data', chunk => {
  input += chunk;
});

process.stdin.on('end', () => {
  let event = {};
  if (input.length > 0) {
    try {
      const parsed = JSON.parse(input);
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        event = parsed;
      }
    } catch (error) {
      process.stderr.write(`session-save: invalid hook event JSON: ${error.message}\n`);
      process.exitCode = 1;
      return;
    }
  }

  const entry = {
    timestamp: typeof event.timestamp === 'string' && event.timestamp.length > 0
      ? event.timestamp
      : new Date().toISOString(),
    event: typeof event.hook_event_name === 'string' && event.hook_event_name.length > 0
      ? event.hook_event_name
      : 'SubagentStop',
    agent: typeof event.agent_type === 'string' && event.agent_type.length > 0
      ? event.agent_type
      : null,
    sessionId: typeof event.session_id === 'string' && event.session_id.length > 0
      ? event.session_id
      : null
  };

  fs.mkdirSync(sessionsDir, { recursive: true });
  fs.appendFileSync(logPath, `${JSON.stringify(entry)}\n`, 'utf8');
});
