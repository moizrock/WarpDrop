# WarpDrop Cloud Sync Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Strict Multi-Tenant Ownership (`/users/{userId}/transfer_records/{recordId}`)**:
   - Every transfer record document belongs exclusively to the authenticated Google user whose `request.auth.uid == userId`.
   - The document's internal `userId` field MUST equal `request.auth.uid` and `userId` path variable on `create` and remain immutable on `update`.
2. **Path & Document ID Hardening**:
   - Both `{userId}` and `{recordId}` must satisfy `isValidId(id)` (`id is string && id.size() >= 1 && id.size() <= 128 && id.matches('^[a-zA-Z0-9_\\-]+$')`).
3. **Strict Schema & Temporal Integrity**:
   - Required fields: `userId`, `fileName`, `sizeBytes`, `category`, `direction`, `peerDeviceName`, `sha256Checksum`, `status`, `createdAt`, `updatedAt`.
   - `createdAt` and `updatedAt` MUST be valid server timestamps (`is timestamp && <= request.time`), and `createdAt` is strictly immutable on `update`.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read/List**: Calling `get` or `list` on `/users/alice_123/transfer_records` with `request.auth == null`.
2. **Cross-User Read Scraping**: User `bob_456` reading `/users/alice_123/transfer_records/rec_1`.
3. **Identity Spoofing on Create**: User `alice_123` writing to `/users/alice_123/transfer_records/rec_1` with `{"userId": "bob_456"}`.
4. **Ghost Field Injection (Shadow Update)**: Sending an extra undeclared field `{"isAdmin": true}` on `create` or `update`.
5. **Immutable Field Mutation**: Attempting to mutate `createdAt` or `userId` during an `update`.
6. **Oversized String Resource Exhaustion**: Setting `fileName` to a 10,000-character string (> 255 limit).
7. **Negative Byte Size Poisoning**: Setting `sizeBytes: -1024`.
8. **Invalid Enum Value for Direction**: Setting `direction: "BROADCAST"` (only `"SENT"` or `"RECEIVED"` allowed).
9. **Invalid Enum Value for Status**: Setting `status: "HACKED"` (only `"PENDING"`, `"IN_PROGRESS"`, `"COMPLETED"`, `"PAUSED"` allowed).
10. **Future Timestamp Spoofing**: Submitting a client timestamp in the future (`createdAt > request.time`).
11. **Type Confusion on Update**: Updating `sizeBytes` to `"1024"` (string instead of int).
12. **Cross-User Delete**: User `bob_456` attempting to delete `/users/alice_123/transfer_records/rec_1`.
