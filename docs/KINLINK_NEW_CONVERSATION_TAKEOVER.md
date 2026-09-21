# KINLINK — NEW CONVERSATION TAKEOVER v2

Use exactly `KINLINKGO`.

On takeover, load durable engineering + communication + delivery-security state first. Do not ask the user to reconstruct the previous chat.

Mandatory order:
1. reconcile any unclosed tranche;
2. load `.project-memory/DELIVERY_SECURITY_POLICY.json`;
3. Gmail START + provider ACK + durable START receipt;
4. before any install instruction, verify it points only to `KINLINK/INSTALLER/KINLINK_INSTALL_NOW.apk`;
5. work from `NEXT_UNCOMMITTED_ACTION.md`;
6. never bypass platform/security controls; checkpoint and resume instead;
7. Gmail END + provider ACK + durable END_ACK before final app reply.

Current truth:
- installed: 0.7.2 / versionCode 10;
- candidate: 0.7.3-dev / versionCode 11;
- exact head: `2aeac4cf801e60986c33347676e12310790d1d98`;
- install file ID: `1dj2j2zhLmUPb63NVIaMx0jElpNdb04ky`;
- SHA-256: `d268542a4e6d4a4a5869deddd71bf0aec32c46386d2193cf96da6b825e68877d`;
- signer: `2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3`;
- readback: PASS.

Rollback is isolated in `ROLLBACK_CANONICAL_DO_NOT_INSTALL` and is never a normal update source.

P0 learned regression: cryptographic validity alone is insufficient. Human delivery also requires package identity + monotonic versionCode + signer continuity + exact hash/readback + single-path exposure.
