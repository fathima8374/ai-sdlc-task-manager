'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { test } = require('node:test');

const source = path.resolve(__dirname, '../hooks/session-save.js');

function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'ads-hook-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  const hooks = path.join(root, '.github', 'hooks');
  fs.mkdirSync(hooks, { recursive: true });
  const script = path.join(hooks, 'session-save.js');
  fs.copyFileSync(source, script);
  return {
    root,
    log: path.join(root, 'sessions', '.hook-log.jsonl'),
    run(input) {
      return spawnSync(process.execPath, [script], { input, encoding: 'utf8' });
    }
  };
}

test('records only the supported completion metadata', t => {
  const f = fixture(t);
  const event = {
    timestamp: '2026-11-16T10:46:08.243Z',
    hook_event_name: 'SubagentStop',
    agent_type: 'verification',
    session_id: 'test-session',
    prompt: 'PRIVATE_INPUT_MUST_NOT_BE_LOGGED',
    token: 'PRIVATE_TOKEN_MUST_NOT_BE_LOGGED'
  };
  const result = f.run(JSON.stringify(event));
  assert.equal(result.status, 0, result.stderr);
  const raw = fs.readFileSync(f.log, 'utf8');
  assert.deepEqual(JSON.parse(raw), {
    timestamp: event.timestamp, event: 'SubagentStop',
    agent: 'verification', sessionId: 'test-session'
  });
  assert.ok(!raw.includes('PRIVATE_'));
});

test('empty input uses a timestamp and optional metadata defaults', t => {
  const f = fixture(t);
  assert.equal(f.run('').status, 0);
  const entry = JSON.parse(fs.readFileSync(f.log, 'utf8'));
  assert.ok(Number.isFinite(Date.parse(entry.timestamp)));
  assert.equal(entry.event, 'SubagentStop');
  assert.equal(entry.agent, null);
  assert.equal(entry.sessionId, null);
});

test('invalid optional field types do not leak into metadata', t => {
  const f = fixture(t);
  assert.equal(f.run('{"timestamp":5,"agent_type":{},"session_id":[],"hook_event_name":""}').status, 0);
  const entry = JSON.parse(fs.readFileSync(f.log, 'utf8'));
  assert.equal(entry.agent, null);
  assert.equal(entry.sessionId, null);
  assert.equal(entry.event, 'SubagentStop');
});

test('malformed JSON fails without creating an audit log', t => {
  const f = fixture(t);
  const result = f.run('{"token":"PRIVATE_INPUT_MUST_NOT_BE_ECHOED"');
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /invalid hook event JSON/);
  assert.ok(!result.stderr.includes('PRIVATE_INPUT'));
  assert.ok(!fs.existsSync(f.log));
});

test('a non-object event is rejected rather than recorded as success', t => {
  const f = fixture(t);
  for (const input of ['null', '[]', '"event"', '42']) {
    const result = f.run(input);
    assert.notEqual(result.status, 0, input);
    assert.ok(!fs.existsSync(f.log));
  }
});

test('multiple completions append parseable JSONL without replacing history', t => {
  const f = fixture(t);
  assert.equal(f.run('{"session_id":"one"}').status, 0);
  assert.equal(f.run('{"session_id":"two"}').status, 0);
  const entries = fs.readFileSync(f.log, 'utf8').trim().split('\n').map(JSON.parse);
  assert.deepEqual(entries.map(entry => entry.sessionId), ['one', 'two']);
});

test('hook does not mutate authoritative workflow state', t => {
  const f = fixture(t);
  fs.mkdirSync(path.join(f.root, 'sessions'));
  const statePath = path.join(f.root, 'sessions', 'workflow-state.json');
  const original = '{"humanApproval":{"verification":{"status":"pending"}}}\n';
  fs.writeFileSync(statePath, original);
  assert.equal(f.run('{"agent_type":"verification"}').status, 0);
  assert.equal(fs.readFileSync(statePath, 'utf8'), original);
});

test('filesystem failure is nonzero and is not reported as saved', t => {
  const f = fixture(t);
  fs.writeFileSync(path.join(f.root, 'sessions'), 'not a directory');
  const result = f.run('{}');
  assert.notEqual(result.status, 0);
  assert.ok(result.stderr.length > 0);
});
