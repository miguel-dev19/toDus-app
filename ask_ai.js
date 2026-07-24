const axios = require('axios');
const fs = require('fs');

const instruction = process.env.INSTRUCTION || 'fix this issue';
const title = process.env.TITLE || 'Issue';
const repoFiles = fs.readdirSync('.', { recursive: true })
  .filter(f => f.endsWith('.kt'))
  .slice(0, 15)
  .join(', ');

const prompt = `Eres un desarrollador Kotlin/Android experto. Debes resolver este issue del proyecto toDus-app.

TITULO: ${title}
INSTRUCCION: ${instruction}
ARCHIVOS: ${repoFiles}

Responde en este formato exacto:
ANALYSIS: (breve analisis)
COMMIT_MSG: (mensaje para el commit)`;

axios.get('https://api.alyacore.xyz/ai/copilot', {
  params: { text: prompt, key: 'oboe' },
  timeout: 60000
}).then(res => {
  const text = res.data.result || res.data.text || res.data.response || JSON.stringify(res.data);
  fs.writeFileSync('ai_response.txt', text);
  console.log('OK');
}).catch(err => {
  fs.writeFileSync('ai_response.txt', 'ANALYSIS: Error contacting AI\nCOMMIT_MSG: Fix: ' + instruction);
  console.log('FALLBACK');
});
