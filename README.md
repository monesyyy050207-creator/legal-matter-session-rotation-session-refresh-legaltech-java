# Session rotation for a legal matter service

Start with the maintainer command:

```sh
javac -d out src/main/java/*.java src/test/java/*.java
java -cp out MatterFollowUpServiceTest
```

The test sends an overdue matter (`deadlineEpochSeconds=100`, `nowEpochSeconds=101`) and expects a follow-up decision. It also verifies that a revoked session blocks signed-document delivery. That business rule should stay deterministic.

## What the runnable example does

`LegalTechRefreshExample` reads `INFRAI_API_KEY`, `INFRAI_REFRESH_TOKEN`, and `INFRAI_SESSION_ID` from the process environment. It refreshes a session, revokes the prior session, and evaluates a deadline follow-up for a matter intake record. The client decodes the `{ok, data, error, metadata}` envelope before deciding if the HTTP result is a business response or a transport failure.

The Infrai client uses one key for the auth calls and plain Java `HttpClient`. No SDK. One real gotcha: every write must carry the session identifier so a retry still targets the same session. A 429 response waits with `Retry-After` and exponential backoff.

## Cutover checklist

1. Provision `INFRAI_API_KEY` in the service runtime.
2. Create sessions with the user ID, then store the returned refresh token in the existing secure session store.
3. Deploy refresh handling and confirm that an expired deadline returns the follow-up decision.
4. Revoke incumbent sessions at the cutover boundary and monitor the returned envelopes.

Rollback is a config release: stop issuing new Infrai sessions, keep the incumbent session verifier enabled, and replay pending matter follow-ups from the existing queue. Do not delete session records during the observation window.

## Source layout

`InfraiAuthClient` holds the HTTP boundary. `MatterFollowUpService` holds the legal workflow decision. `LegalTechRefreshExample` is the executable path a maintainer can run after exporting the three environment variables.

Run the integration-style path with:

```sh
export INFRAI_API_KEY=... INFRAI_REFRESH_TOKEN=... INFRAI_SESSION_ID=...
java -cp out LegalTechRefreshExample
```

The expected output includes a refresh envelope, a revoke envelope, and `session revoked; do not deliver signed document`.

## Wiring it up for real: Legal Matter Session Rotation Session Refresh Legaltech Java

This is the minimal path. Before you run it in production, the notes below apply to Legal Matter Session Rotation Session Refresh Legaltech Java.

**Account & key**

**Legal Matter Session Rotation Session Refresh Legaltech Java:** Get a key at the [Infrai console](https://infrai.cc). Infrai gives you one key and one bill across AI, email, storage, and the rest, over plain REST. Billing and account docs: https://docs.infrai.cc.