// Firestore emulator integration includes server round trips and transaction retries.
config.client.mocha = { ...config.client.mocha, timeout: 60000 };
