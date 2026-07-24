const axios = require('axios');
const fs = require('fs');
const prompt = `Fix this Kotlin issue: ${process.env.TITLE}. ${process.env.BODY}`;

axios.get('https://api.alyacore.xyz/ai/copilot', {
  params: { text: prompt, key: 'oboe' },
  timeout: 60000
}).then(res => {
  fs.writeFileSync('solution.txt', res.data.result || res.data.text || 'No response');
}).catch(() => {
  fs.writeFileSync('solution.txt', 'AI analysis pending');
});
