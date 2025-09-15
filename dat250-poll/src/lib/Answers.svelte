<script>

    export let pollId;


    let poll = null;
    let selectedOptionId = null;
    let hasVoted = false; // Renamed to match the usage in the HTML
    let isLoading = false;
    let error = null;


    const userId = 'stub_stubernes';


    async function fetchPoll() {
      isLoading = true;
      error = null;
      try {

        const response = await fetch(`http://localhost:8080/polls/${pollId}`);
        if (!response.ok) {

          throw new Error("Failed to fetch poll");
        }
        poll = await response.json();
      } catch (err) {
        error = err.message;
      } finally {
        isLoading = false;
      }
    }


    $: if (pollId) {
        fetchPoll();
    }



    function selectOption(optionId) {
      if (!hasVoted) {
        selectedOptionId = optionId;
      }
    }


    async function submitVote() {
      // Corrected typo
      if (!selectedOptionId || hasVoted) return;

      isLoading = true;
      error = null;

      const voteData = {
          userId: userId,
          optionId: selectedOptionId
      };

      try {
        const response = await fetch(`http://localhost:8080/polls/${pollId}/votes`, {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: JSON.stringify(voteData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to cast vote');
        }

        hasVoted = true;
        console.log("Voted successfully");
      } catch (err) {
        error = err.message;
      } finally {
        isLoading = false;
      }
    }
</script>

<main class="vote-container">
  {#if isLoading}
    <p>Loading poll...</p>
  {:else if error}
    <p class="error">Error: {error}</p>
  {:else if poll}
    <h2>{poll.question}</h2>
    <p>Please select an option below.</p>

    <div class="options">
      {#each poll.options as option (option.id)}
        <button
          class="option"
          class:selected={selectedOptionId === option.id}
          on:click={() => selectOption(option.id)}
          disabled={hasVoted}
        >
          {option.text}
        </button>
      {/each}
    </div>

    {#if selectedOptionId && !hasVoted}
      <button class="submit-btn" on:click={submitVote} disabled={isLoading}>
        {isLoading ? 'Submitting...' : 'Submit Vote'}
      </button>
    {:else if hasVoted}
      <p class="success-message">Thank you for voting!</p>
    {/if}
  {:else}
    <p>Poll not found.</p>
  {/if}
</main>