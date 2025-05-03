#!/usr/bin/env node

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const jarPath = path.join(__dirname, 'cli-all.jar');

if (!fs.existsSync(jarPath)) {
    console.error(`❌ Jar file not found at ${jarPath}`);
    process.exit(1);
}

const args = process.argv.slice(2).join(' ');

try {
    const command = `java -jar "${jarPath}" ${args}`;
    const output = execSync(command, { encoding: 'utf8' });
    console.log(output);
} catch (error) {
    console.error(error.stderr || error.stdout || error.message);
    process.exit(error.status || 1);
}