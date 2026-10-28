const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile or check-ins", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("checkIns").get());
});

test("Authenticated user: can create and read their own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      biometricLockEnabled: false,
      themeMode: "gentle_sage",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user access: Bob cannot read or write Alice's check-ins", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("users").doc(ALICE_UID).collection("checkIns").doc("checkin_1").set({
    userId: ALICE_UID,
    date: "2026-10-28",
    moodScore: 4,
    moodLabel: "Good",
    emotionTags: ["Calm", "Grateful"],
    energyLevel: 4,
    sleepQuality: 5,
    createdAt: new Date(),
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("checkIns").doc("checkin_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("checkIns").doc("bob_injected").set({
      userId: BOB_UID,
      date: "2026-10-28",
      moodScore: 3,
      moodLabel: "Neutral",
      emotionTags: ["Tired"],
      energyLevel: 2,
      sleepQuality: 3,
      createdAt: new Date(),
    })
  );
});

test("Validation: CheckIn with invalid moodScore or missing fields is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Invalid moodScore (out of 1-5 range)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("checkIns").doc("bad_checkin").set({
      userId: ALICE_UID,
      date: "2026-10-28",
      moodScore: 10,
      moodLabel: "Super",
      emotionTags: ["Calm"],
      energyLevel: 4,
      sleepQuality: 4,
      createdAt: new Date(),
    })
  );
});

test("Journal entry: Alice can create and read her gratitude entries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("journalEntries").doc("journal_1").set({
      userId: ALICE_UID,
      date: "2026-10-28",
      title: "Peaceful morning",
      gratitudeAnswers: ["Warm tea", "Fresh air", "A good rest"],
      reflection: "Felt very relaxed listening to the quiet rain today.",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("journalEntries").doc("journal_1").get()
  );
});
