# Catch-All Email Verifier - Java Library

[![Maven Central](https://img.shields.io/maven-central/v/io.enrow/catch-all-email-verifier.svg)](https://central.sonatype.com/artifact/io.enrow/catch-all-email-verifier)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

Verify emails on catch-all domains with deterministic verification. Most verifiers mark catch-all emails as "risky" or "unknown" -- this one tells you if the specific mailbox actually exists.

Powered by [Enrow](https://enrow.io) -- deterministic email verification, not probabilistic.

## The catch-all problem

A catch-all (or accept-all) domain is configured to accept mail sent to any address at that domain, whether or not the specific mailbox exists. This means `anything@company.com` will not bounce at the SMTP level, so traditional email verifiers cannot distinguish real inboxes from non-existent ones. They return "accept-all", "risky", or "unknown" and leave you guessing.

Enrow uses deterministic verification techniques that go beyond SMTP handshake checks, resolving the actual mailbox existence on catch-all domains. The result is a clear valid/invalid verdict instead of an inconclusive shrug.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.enrow</groupId>
    <artifactId>catch-all-email-verifier</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.enrow:catch-all-email-verifier:1.0.0'
```

Requires Java 11+. Only dependency: Gson.

## Simple Usage

```java
import io.enrow.catchallverifier.CatchAllVerifier;
import java.util.Map;

Map<String, Object> verification = CatchAllVerifier.verify("your_api_key", Map.of(
    "email", "tcook@apple.com"
));

Map<String, Object> result = CatchAllVerifier.get("your_api_key", (String) verification.get("id"));

System.out.println(result.get("email"));         // tcook@apple.com
System.out.println(result.get("qualification")); // valid
```

`CatchAllVerifier.verify` returns a verification ID. The verification runs asynchronously -- call `CatchAllVerifier.get` to retrieve the result once it is ready. You can also pass a `webhook` URL inside a `settings` map to get notified automatically.

## Bulk verification

```java
Map<String, Object> batch = CatchAllVerifier.verifyBulk("your_api_key", Map.of(
    "verifications", java.util.List.of(
        Map.of("email", "tcook@apple.com"),
        Map.of("email", "satya@microsoft.com"),
        Map.of("email", "jensen@nvidia.com")
    )
));

// batch.get("batchId"), batch.get("total"), batch.get("status")

Map<String, Object> results = CatchAllVerifier.getBulk("your_api_key", (String) batch.get("batchId"));
// results.get("results") -- list of result maps
```

Up to 5,000 verifications per batch. Pass a `webhook` URL inside `settings` to get notified when the batch completes.

## Error handling

```java
try {
    CatchAllVerifier.verify("bad_key", Map.of("email", "test@test.com"));
} catch (RuntimeException e) {
    // e.getMessage() contains the API error description
    // Common errors:
    // - "Invalid or missing API key" (401)
    // - "Your credit balance is insufficient." (402)
    // - "Rate limit exceeded" (429)
}
```

## Getting an API key

Register at [app.enrow.io](https://app.enrow.io) to get your API key. You get **50 free credits** (= 200 verifications) with no credit card required. Each verification costs **0.25 credits**.

Paid plans start at **$17/mo** for 1,000 credits up to **$497/mo** for 100,000 credits. See [pricing](https://enrow.io/pricing).

## Documentation

- [Enrow API documentation](https://docs.enrow.io)
- [Full Enrow SDK](https://github.com/enrow/enrow-java) -- includes email finder, phone finder, reverse email lookup, and more

## License

MIT -- see [LICENSE](LICENSE) for details.
