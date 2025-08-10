#!/usr/bin/env node

const { execSync } = require('child_process');

try {
  console.log('Running TypeScript error suppression script...');
  execSync('node scripts/suppress-ts-errors.js', { stdio: 'inherit' });
  console.log('TypeScript suppression completed!');
} catch (error) {
  console.error('Error running suppression script:', error.message);
}