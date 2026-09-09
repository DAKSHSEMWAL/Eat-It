import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";

describe("Realtime Database Security Rules", () => {
  let testEnv: RulesTestEnvironment;

  beforeAll(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: "eat-it-test-project",
      database: {
        host: "127.0.0.1",
        port: 9000,
      },
    });
  });

  afterAll(async () => {
    if (testEnv) {
      await testEnv.cleanup();
    }
  });

  beforeEach(async () => {
    await testEnv.clearDatabase();
  });

  test("Anonymous users can read Category and Food catalog", async () => {
    const unauthDb = testEnv.unauthenticatedContext().database();
    await assertSucceeds(unauthDb.ref("Category").once("value"));
    await assertSucceeds(unauthDb.ref("Food").once("value"));
  });

  test("Clients cannot write directly to Requests node (must use placeOrder Cloud Function)", async () => {
    const userDb = testEnv.authenticatedContext("user123").database();
    await assertFails(userDb.ref("Requests/ORD_123").set({
      userId: "user123",
      totalPaise: 5000,
    }));
  });

  test("Users can read their own orders under Requests", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.database().ref("Requests/ORD_123").set({
        userId: "user123",
        name: "Daksh",
        phone: "9876543210",
        address: "123 Main Street",
        totalPaise: 5000,
        status: "0",
        foods: [],
        createdAt: 1234567890,
      });
    });

    const ownerDb = testEnv.authenticatedContext("user123").database();
    await assertSucceeds(ownerDb.ref("Requests/ORD_123").once("value"));

    const strangerDb = testEnv.authenticatedContext("stranger456").database();
    await assertFails(strangerDb.ref("Requests/ORD_123").once("value"));
  });
});
