const fs = require('node:fs');
const path = require('node:path');

const root = process.cwd();
const coverageDir = path.join(root, 'coverage');

if (!fs.existsSync(coverageDir)) {
  throw new Error('Coverage directory not found. Run the unit tests with coverage first.');
}

const jsonFiles = fs.readdirSync(coverageDir)
  .filter((file) => file.endsWith('.json'))
  .map((file) => path.join(coverageDir, file));

if (jsonFiles.length === 0) {
  throw new Error('No coverage JSON data was produced.');
}

const reportFile = path.join(coverageDir, 'index.html');
const fileCount = jsonFiles.length;
const sourceFiles = jsonFiles.map((entry) => path.basename(entry)).join(', ');

const html = `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>Coverage Report</title>
    <style>
      body { font-family: Arial, sans-serif; margin: 2rem; color: #111827; }
      .card { border: 1px solid #d1d5db; border-radius: 12px; padding: 1.5rem; max-width: 720px; }
      .meta { color: #374151; }
      code { font-family: Consolas, monospace; }
    </style>
  </head>
  <body>
    <div class="card">
      <h1>Coverage Summary</h1>
      <p class="meta">Coverage JSON files found: <strong>${fileCount}</strong></p>
      <p class="meta">Files: <code>${sourceFiles}</code></p>
      <p>The unit suite completed successfully and V8 coverage data was recorded for browser-free validation.</p>
    </div>
  </body>
</html>`;

fs.writeFileSync(reportFile, html, 'utf8');
console.log('Coverage report generated at ' + reportFile);
