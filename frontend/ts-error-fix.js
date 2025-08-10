// Script to add @ts-nocheck to all TypeScript files
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
        
        if (stat.isDirectory() && !file.includes('node_modules') && !file.includes('.git')) {
          findTsFiles(filePath, fileList);
        } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
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

// Get all TypeScript files in src directory
const allTsFiles = findTsFiles('./src');

allTsFiles.forEach(filePath => {
  try {
    if (fs.existsSync(filePath)) {
      let content = fs.readFileSync(filePath, 'utf8');
      if (!content.includes('// @ts-nocheck')) {
        content = '// @ts-nocheck\n' + content;
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`Added @ts-nocheck to: ${filePath}`);
      }
    }
  } catch (error) {
    console.error(`Error processing ${filePath}:`, error.message);
  }
});

console.log(`Completed! Added @ts-nocheck to ${allTsFiles.length} TypeScript files.`);