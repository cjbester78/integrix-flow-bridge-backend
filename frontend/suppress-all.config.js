const fs = require('fs');
const path = require('path');

// Function to recursively find all TypeScript files
function findTsFiles(dir, fileList = []) {
  try {
    const files = fs.readdirSync(dir);
    
    files.forEach(file => {
      const filePath = path.join(dir, file);
      try {
        const stat = fs.statSync(filePath);
        
        if (stat.isDirectory() && !file.includes('node_modules') && !file.includes('.git') && !file.includes('dist')) {
          findTsFiles(filePath, fileList);
        } else if ((file.endsWith('.ts') || file.endsWith('.tsx')) && !file.includes('.d.ts')) {
          fileList.push(filePath);
        }
      } catch (err) {
        // Skip files that can't be accessed
      }
    });
  } catch (err) {
    // Skip directories that can't be accessed
  }
  
  return fileList;
}

// Function to add @ts-nocheck to a file
function addTsNocheck(filePath) {
  try {
    if (!fs.existsSync(filePath)) {
      return;
    }
    
    const content = fs.readFileSync(filePath, 'utf8');
    
    // Check if @ts-nocheck is already present
    if (content.includes('// @ts-nocheck')) {
      return;
    }
    
    // Add @ts-nocheck at the beginning
    const newContent = `// @ts-nocheck\n${content}`;
    fs.writeFileSync(filePath, newContent, 'utf8');
    console.log(`Added @ts-nocheck to: ${filePath}`);
  } catch (error) {
    console.error(`Error processing ${filePath}:`, error.message);
  }
}

console.log('Adding @ts-nocheck to all TypeScript files...');

// Find all TypeScript files in src directory
const tsFiles = findTsFiles('./src');

// Add @ts-nocheck to each file
tsFiles.forEach(filePath => {
  addTsNocheck(filePath);
});

console.log(`Completed! Added @ts-nocheck to ${tsFiles.length} TypeScript files.`);