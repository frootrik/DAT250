

<script>

    let pollName = '';
    let options = [{ text: ''}, {text: ''}];

    function addOption(){
        options = [...options, { text: ''}];
    }

    async function handleSubmit(){

        const pollData = {
        ownerUserId: 'stub_stubernes',
        question: pollName,
        options: options.map(opt => opt.text)
        };
        try {
            const response = await fetch('http://localhost:8080/polls', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(pollData)

            });
            if (!response.ok){
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to make poll');
            }
            const createdPoll = await response.json();
            console.log('Poll created successfully:', createdPoll);
            } catch (error) {
                console.error('Error creating poll:', error);
    }
  }

</script>

<form on:submit|preventDefault={handleSubmit}>
    <label for="poll-name">Poll Name:</label>
    <input type="text" id="poll-name" bind:value={pollName}>

    <p>Options:</p>
    {#each options as option}
        <input type="text" bind:value={option.text} placeholder="Option Text" />
   {/each}

   <button type="button" on:click={addOption}>Add Option</button>
   <button type="submit">Create Poll</button>
</form>

<style>
  form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    padding: 1.5rem;
    border: 1px solid #ccc;
    border-radius: 8px;
    background-color: #f9f9f9;
  }
  label {
    font-weight: bold;
    color: #333;
  }
  input[type="text"] {
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
  }
  button {
    padding: 0.75rem 1.5rem;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
</style>