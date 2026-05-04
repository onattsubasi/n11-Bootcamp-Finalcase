const fs = require('fs');
const path = require('path');

const walkSync = (dir, filelist = []) => {
  fs.readdirSync(dir).forEach(file => {
    const filePath = path.join(dir, file);
    if (fs.statSync(filePath).isDirectory()) {
      filelist = walkSync(filePath, filelist);
    } else {
      filelist.push(filePath);
    }
  });
  return filelist;
};

const apiFiles = walkSync(path.join(__dirname, 'src', 'features'))
  .filter(f => f.endsWith('Api.js'));

apiFiles.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  
  // Check if it's already using unwrapApiResponse or unwrapPage
  if (content.includes('unwrapApiResponse') || content.includes('unwrapPage')) {
    // If it's catalogApi.js or checkoutApi.js or searchApi.js, we might have already done it
    return;
  }

  // Find how many levels deep we are to import from lib/utils/api
  // e.g. src\features\auth\api\authApi.js -> depth 4 from src
  // We want to go back to src: ../../../lib/utils/api
  const depth = file.split(path.sep).length - path.join(__dirname, 'src').split(path.sep).length;
  const importPath = '../'.repeat(depth - 1) + 'lib/utils/api';
  
  const importStatement = `import { unwrapApiResponse } from '${importPath}';\n`;
  
  // Add import after other imports
  const lastImportIndex = content.lastIndexOf('import ');
  if (lastImportIndex !== -1) {
    const endOfLastImport = content.indexOf('\n', lastImportIndex);
    content = content.slice(0, endOfLastImport + 1) + importStatement + content.slice(endOfLastImport + 1);
  } else {
    content = importStatement + content;
  }

  // Replace `return data;` with `return unwrapApiResponse(data);`
  // And `return res.data;` with `return unwrapApiResponse(res.data);`
  content = content.replace(/return\s+data\s*;/g, 'return unwrapApiResponse(data);');
  content = content.replace(/return\s+res\.data\s*;/g, 'return unwrapApiResponse(res.data);');

  fs.writeFileSync(file, content, 'utf8');
  console.log(`Updated ${file}`);
});
