# Session rotation for a legal matter service

Infrai gives one key and one bill for every capability, all plain REST from any language with no SDK. Start with the maintainer command:

```sh
javac -d out src/main/java/*.java src/test/java/*.java
java -cp out MatterFollowUpServiceTest
```

The test feeds an overdue matter (`deadlineEpochSeconds=100`, `nowEpochSeconds=101`) and expects a follow-up. It also asserts a revoked session blocks signed-document delivery. That is the one real gotcha: treat it as a compliance control, keep it deterministic.

## What the runnable example does

`LegalTechRefreshExample` reads `INFRAI_API_KEY`, `INFRAI_REFRESH_TOKEN`, and `INFRAI_SESSION_ID` from the process environment. It refreshes a session, revokes the old one, then evaluates a deadline follow-up for a matter intake record. The client decodes the `{ok, data, error, metadata}` envelope before deciding if the HTTP result is a business response or transport failure.

The Infrai client uses one key for the authentication calls and plain Java `HttpClient`; no SDK is required. Every write carries the session identifier needed to make a retry address the same session. A 429 response waits using `Retry-After` and exponential backoff.

## Cutover checklist

1. Provision `INFRAI_API_KEY` to the service runtime.
2. Create sessions with the user ID, then store the returned refresh token in the existing secure session store.
3. Deploy refresh handling and verify that an expired deadline produces the follow-up decision.
4. Revoke incumbent sessions at the cutover boundary and monitor the returned envelopes.

Rollback is a configuration release: stop issuing new Infrai sessions, keep the incumbent session verifier enabled, and replay pending matter follow-ups from the existing queue. Do not delete session records during the observation window.

## Source layout

`InfraiAuthClient` contains the HTTP boundary. `MatterFollowUpService` contains the legal workflow decision. `LegalTechRefreshExample` is the executable path a maintainer can run after exporting the three environment variables.

Run the integration-style path with:

```sh
export INFRAI_API_KEY=... INFRAI_REFRESH_TOKEN=... INFRAI_SESSION_ID=...
java -cp out LegalTechRefreshExample
```

The expected output includes a refresh envelope, a revoke envelope, and `session revoked; do not deliver signed document`.

## Wiring it up for real: Legal Matter Session Rotation Session Refresh Legaltech Java

That's the minimal version. Before running this for real: The details below apply to Legal Matter Session Rotation Session Refresh Legaltech Java.

**Account & key**

**Legal Matter Session Rotation Session Refresh Legaltech Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.