# Wish Sparks — developer task

## Goal and scope

Implement swipe-based wish discovery for paired users, following the [designer task](wish-sparks-designer-task.md) and the resulting Figma handoff. The final feature name and screen title are **Wish Sparks**; use **Explore ideas** for entry and **Both liked** for mutual suggestions. Identify the feature as Wish Sparks in the entry invitation.

This is a future implementation brief. The current project has shared wishes and memories but no discovery catalog, individual votes, or mutual-interest model.

The first version includes a curated catalog of approximately 60 localized ideas, existing category filters, a swipe deck with equivalent buttons, session undo, a mutual-interest collection, and conversion through the existing wish form. Support Android, iOS, Web, and the Desktop demo. Guest/unpaired discovery, AI recommendations, external content services, notifications, and advanced filters are out of scope.

## Existing integration points

- `shared/.../ui/WishesList.kt`: populated-list and empty-list discovery entry points.
- `shared/.../ui/PairedNavigation.kt`: discovery and mutual-collection routes within Wishes.
- `shared/.../ui/WishForm.kt` and `WishFormState.kt`: prefilled draft and conversion save behavior.
- `shared/.../ui/AdaptiveLayout.kt`: existing mobile, tablet, and expanded layouts.
- `core/model/.../PairedSpace.kt`: `WishCategory`, `WishDraft`, `Wish`, and completion models.
- `core/data/.../repository/WishesRepository.kt`: existing wish lifecycle interface.
- Platform cloud adapters, Firestore rules, resource localization, and JVM screenshot tests.

Paths above abbreviate the existing package directories. Inspect current implementations before choosing exact API changes. Reuse design-system components, bundled category art, and existing form validation: title required and at most 80 characters; description at most 500 characters.

## Product behavior

1. A paired user opens discovery from Wishes; the three main tabs remain unchanged.
2. Show an eligible idea from the selected category or all categories.
3. Left swipe or **Not now** records a skip; right swipe or **I'd love this** records a like. A cancelled drag records nothing.
4. Both partners liking the same idea makes it available in **Both liked**. Participation is asynchronous.
5. **Make it a wish** opens an editable draft populated from the idea, with no date selected.
6. Saving creates one shared wish. Cancelling creates neither a wish nor a conversion marker.
7. Converted ideas offer **View wish**. Completed wishes continue through the existing memory experience.

Individual choices must remain private until mutual interest exists. Neither partner may read the other's vote documents. Do not expose pending responses, skips, or partner-specific like lists.

Undo restores the user's previous choice for the latest successful vote in the current deck session. If this removes mutual interest, remove the unconverted mutual suggestion. It must never remove or modify an already-created wish. Reopening discovery need not preserve the undo stack.

## Catalog and deck selection

Use stable idea IDs independent of translated text. A catalog entry needs an ID, content version, localized title and description, existing category, supported cover reference, and enabled state. Optional display tags must also be localized. Prefer bundled category art for the first version; do not persist temporary image URLs.

Keep content and UI available in English, Russian, and Kazakh, using the app's system-language behavior and explicit fallback. Seed examples across all six categories; gift ideas should work as mutual activities, such as choosing books for each other.

Both members must receive an overlapping catalog. Start with a stable couple-specific ordering, filtered by the current user's choices and selected category. Do not select or label cards in a way that reveals a partner's private vote. Exclude already-converted ideas from the normal deck.

Do not immediately recycle skipped cards. Offer an explicit **Revisit skipped ideas** action after exhaustion; revisiting must not erase prior choices until the user changes them. Preserve choices across restarts and devices. Category changes must not reset progress.

## Data and backend design

Keep catalog entries and discovery choices separate from `Wish`. Introduce a discovery repository with observable deck/choice/mutual state and explicit failure handling. Provide a deterministic in-memory implementation for Desktop and tests.

The logical records should cover:

| Record | Required information |
| --- | --- |
| Idea | Stable ID, version, localized content, category, artwork, enabled state. |
| Personal choice | Couple ID, user ID, idea ID, like/skip, revision or update metadata. Absence means unanswered. |
| Mutual suggestion | Couple ID and idea ID, current mutual status, optional linked wish ID. |
| Conversion | A unique couple-and-idea key linked to the created wish. |

Choose physical paths after reviewing the existing schema. Scope votes to the couple so preferences from a former pairing do not leak into a new relationship.

**Privacy is a backend requirement.** Client-side hiding is insufficient. Design a trusted backend operation that can evaluate both private choices and publish only mutual results. For example, an authenticated backend function can update a user's vote and recompute mutual state transactionally. The project currently needs new backend capability for this behavior; do not assume existing wish adapters provide it. Document the chosen API, deployment requirements, and authorization rules.

Authorize every mutation against current couple membership and the authenticated user's identity. Clients must not forge another user's choice or directly manufacture mutual results. Verify idea IDs and input values against trusted catalog data. Use revisions or equivalent concurrency control so delayed operations cannot overwrite newer choices unnoticed.

Wish conversion must be atomic and idempotent for a couple-and-idea pair. Concurrent saves and retries must return the same wish rather than create duplicates. Revalidate mutual eligibility when saving; if interest was withdrawn, retain the edited draft and explain why conversion cannot complete. A saved wish remains independent of subsequent vote changes.

Store source idea provenance separately from editable wish text. The saved wish is a snapshot; future catalog edits must not rewrite it. Assign the saving user as author using the current attribution model.

Define linked-wish deletion explicitly: recommended first-version behavior is to retain conversion history, show that the wish was removed, and avoid silently recreating or resurfacing it. An explicit recreation flow is outside this version. Deleted or disabled catalog content must not break existing wishes.

## UI, lifecycle, and failures

- Implement the approved entry, deck, mutual acknowledgment, collection, and conversion states using shared Compose UI.
- Support swipe cancellation, labelled buttons, keyboard navigation, screen readers, large text, and reduced motion. All functionality must be available without gestures.
- Keep cards bounded on wide windows and follow the existing responsive navigation.
- Restore deck progress from persisted choices; returning from details or the form should not reset the active category unnecessarily.
- Handle loading, empty categories, exhaustion, retry, failed votes, and failed conversion explicitly.
- Online writes are sufficient for version one. Cached content may remain visible, but do not report offline writes as saved. If UI advances optimistically, roll back coherently on failure.
- Clear private state and listeners on sign-out/account switch. Recheck membership and stop discovery when pairing access is lost.
- Keep mutual acknowledgments from repeatedly firing on ordinary recomposition or snapshot replay; the collection remains the durable place to find mutual ideas.

## Verification and acceptance criteria

- Both swipe directions and equivalent buttons produce the same persisted result; cancelled gestures produce no vote.
- Filters preserve progress, deck ordering overlaps between partners, and skipped ideas return only through explicit revisit.
- Mutual suggestions appear only after two likes and disappear when an unconverted mutual like is undone.
- A single like never creates a wish. Form cancellation creates nothing; successful conversion preserves edits and optional dates.
- Concurrent conversion, retry after an ambiguous response, and repeated taps create at most one wish per couple-and-idea pair.
- Existing wish editing, completion, restoration, and memories continue working for converted wishes.
- Tests cover account switching, lost pairing, delayed requests, unavailable catalog entries, and failed saves.
- Backend/emulator tests prove that outsiders and partners cannot read another user's private votes or forge mutual state; include direct API attempts, not only UI tests.
- Unit tests cover selection, undo, mutual-state transitions, and conversion idempotency. Add focused integration/UI tests for the full two-account journey and responsive/accessibility behavior.
- Localized catalog data and copy satisfy form limits; layouts handle long strings and large text.

For implementation PRs that change visuals, follow `AGENTS.md`: commit and push, run `generate-screenshots.yml` on the task branch, wait for success, pull the generated reference commit, and review PNG differences before creating the PR. Never commit Windows-generated screenshot references. This documentation-only task does not require screenshot generation.

## Delivery and outcome

Deliver the shared UI and model/repository changes, platform integrations, curated localized catalog, backend/rule changes with deployment instructions, tests, and reviewed CI screenshot references. Record any required backend deployment separately from application builds.

Evaluate discovery by the progression from ideas to saved wishes to completed memories. Do not add analytics infrastructure or transmit individual voting history as part of this scope; any later measurement implementation needs its own data and event design.
