



<script>
    import Answers from './lib/Answers.svelte';
    import Poll from './lib/Poll.svelte';

    const testId = '215158b4-5b7d-4636-8b73-bb0e65e14964';
</script>

<main>
    <h2> Welcome to the Polling App!</h2>
    <Poll />

    <Answers pollId={testId} />
</main>