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

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validPayload(userId, overrides = {}) {
  const pastNow = new Date(Date.now() - 1000);
  return {
    userId,
    fileName: "WarpDrop_4K_Demo.mp4",
    sizeBytes: 104857600,
    category: "VIDEOS",
    direction: "SENT",
    peerDeviceName: "Pixel 9 Pro XL",
    sha256Checksum: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    status: "COMPLETED",
    createdAt: pastNow,
    updatedAt: pastNow,
    ...overrides,
  };
}

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

test("Unauthenticated user: cannot read or write transfer_records", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transfer_records").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transfer_records")
      .doc("rec_1")
      .set(validPayload(ALICE_UID))
  );
});

test("Authenticated owner: can create, get, list, update, and delete own transfer_records", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transfer_records")
    .doc("rec_1");

  await assertSucceeds(docRef.set(validPayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transfer_records")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    docRef.update({
      status: "PAUSED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Authenticated user: cannot read, update, or delete another user's transfer_record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transfer_records")
      .doc("rec_alice")
      .set(validPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDocAsBob = bobDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transfer_records")
    .doc("rec_alice");

  await assertFails(aliceDocAsBob.get());
  await assertFails(
    aliceDocAsBob.update({
      status: "PAUSED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(aliceDocAsBob.delete());
});

test("Shadow Update & Schema Poisoning: rejects ghost fields and invalid status/direction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const colRef = aliceDb.collection("users").doc(ALICE_UID).collection("transfer_records");

  await assertFails(
    colRef.doc("rec_ghost").set(validPayload(ALICE_UID, { isAdmin: true }))
  );
  await assertFails(
    colRef.doc("rec_bad_dir").set(validPayload(ALICE_UID, { direction: "INVALID" }))
  );
  await assertFails(
    colRef.doc("rec_bad_size").set(validPayload(ALICE_UID, { sizeBytes: -50 }))
  );
});
