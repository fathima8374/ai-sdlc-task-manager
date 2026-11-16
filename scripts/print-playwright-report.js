const fs = require('node:fs');
const path = require('node:path');

const reportPath = path.join(process.cwd(), 'playwright-report', 'index.html');

if (!fs.existsSync(reportPath)) {
  console.error('Playwright report not found at ' + reportPath);
  process.exit(1);
}

console.log('Playwright report: ' + reportPath);
