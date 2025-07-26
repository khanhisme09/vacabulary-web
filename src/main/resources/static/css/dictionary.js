document.getElementById('dictionaryForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    const wordInput = document.getElementById('wordInput').value.trim();
    const resultDiv = document.getElementById('dictionaryResult');
    resultDiv.innerHTML = '';

    if (!wordInput) {
        resultDiv.innerHTML = '<div class="alert alert-danger">Please enter a word to search.</div>';
        return;
    }

    try {
        const response = await fetch(`/api/dictionary?word=${encodeURIComponent(wordInput)}`);
        const data = await response.json();

        if (response.ok && data.length > 0) {
            const wordData = data[0];
            let html = `<h4>${wordData.word}</h4>`;
            if (wordData.phonetic) {
                html += `<p><strong>Pronunciation:</strong> ${wordData.phonetic}</p>`;
            }
            wordData.meanings.forEach(meaning => {
                html += `<p><strong>${meaning.partOfSpeech}:</strong> ${meaning.definitions[0].definition}</p>`;
                if (meaning.definitions[0].example) {
                    html += `<p><em>Example:</em> ${meaning.definitions[0].example}</p>`;
                }
            });
            resultDiv.innerHTML = html;
        } else {
            resultDiv.innerHTML = '<div class="alert alert-warning">No definitions found for this word.</div>';
        }
    } catch (error) {
        resultDiv.innerHTML = '<div class="alert alert-danger">Error fetching dictionary data. Please try again later.</div>';
    }
});