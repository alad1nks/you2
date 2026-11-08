# Wish Sparks — designer task

## Goal

Design wish discovery for you2: partners independently swipe through ideas, discover what they both like, and turn those suggestions into shared wishes. Support the existing journey from wish to plan to memory.

This brief describes a proposed feature, not functionality already implemented. Coordinate behavior with the [developer task](wish-sparks-developer-task.md).

## Naming and tone

The final feature name is **Wish Sparks**. Use **Wish Sparks** as the screen title, **Explore ideas** for the entry action, and **Both liked** for mutual suggestions. Identify the feature as Wish Sparks in the entry invitation and design handoff.

Keep the copy warm and low-pressure. Use **Not now** and **I'd love this** for choices. Avoid dating-app terminology, rejection language, competitive scores, and streak pressure.

## Existing product and visual reference

- [you2 Figma file](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2)
- [Our wishes](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2?node-id=1-2)
- [No wishes yet](https://www.figma.com/design/4NzqzLDkOMaaGQGKYhEeuT/you2?node-id=120-2061)
- Design-system section: `63:996`; wishes section: `62:998`.

Reuse the cream surfaces, olive actions, lime highlights, Roboto typography, rounded cards, category illustrations, and existing light/dark themes. Use the established spacing and components. Preserve the Wishes, Memories, and Us navigation.

## First-version scope

Design for signed-in, paired users. Partners can browse at different times. Use a curated catalog of approximately 60 ideas across Adventures, Presents, Plans, Movies, TV series, and Other. Categories should retain their existing names and meaning.

Guest discovery, personal saved ideas before pairing, AI generation, external catalogs, push notifications, and advanced recommendation controls are outside this version.

## Screens and flow

### 1. Entry from Wishes

On a populated list, add a compact invitation below the summary and before category filters, with the action **Explore ideas**. Keep manual wish creation prominent.

In the empty state, offer discovery alongside **Add your first wish**. Explain that ideas can help the couple get started. Do not imply browsing creates wishes automatically.

### 2. Discovery deck

Provide a back action, **Wish Sparks** title, and **Both liked** shortcut. Place category filters above one large card with a subtle preview of the next card. Default to all categories.

Each card contains an illustration, title, short description, and category. Optional descriptive tags such as Outdoors or Low cost may appear only when supplied by the catalog; they are not additional filters in the first version.

Example:

> **A breakfast picnic**
> Bring something warm, find a quiet spot, and start the day slowly.
> Plans · Outdoors · Low cost

Swipe left for **Not now** and right for **I'd love this**. Provide equally capable labelled buttons, plus **Undo** for the latest choice in the current browsing session. A partial or cancelled drag must leave the choice unchanged. Show a brief first-use explanation that choices remain private unless both partners like the idea.

Use a gentle tilt and short transition. Specify a reduced-motion alternative. Keep navigation and text scrolling usable without accidental votes.

### 3. Mutual interest

When both partners like an idea, show a lightweight acknowledgment that does not repeatedly interrupt browsing:

> **A little yes from both of you.**
> You and Mia would love a breakfast picnic.
> **Make it a wish** · Keep exploring

Partners who were absent can find the suggestion later in **Both liked**. Do not show a partner's individual likes, skips, pending responses, or rejection status.

### 4. Both liked collection

Show mutual ideas with enough information to choose one and an action to **Make it a wish**. Once converted, show **View wish** instead. A completed linked wish should open its existing memory/details experience.

Empty-state direction: “Your shared ideas will appear here when you both find something you'd love.” Avoid implying the partner has failed to participate.

### 5. Make it a wish

Open the existing wish form with title, description, category, and cover filled in. Dates remain unset. The user can edit before saving; cancelling creates nothing. Saving creates one shared wish and returns a clear confirmation with access to it.

A like alone never adds to the shared wish list. Undoing a like must not delete a wish already created from the idea.

## Required states and interaction notes

- Initial loading, loading failure with retry, and temporary connection failure during a choice.
- No ideas for the selected category, with a way to return to all categories.
- All available ideas reviewed, with actions to view mutual ideas or explicitly revisit skipped ideas.
- Empty and populated Both liked collections.
- Undo available/unavailable, and mutual interest withdrawn before conversion.
- Conversion in progress, save failure, and another partner having already created the wish.
- Paired access lost while browsing: follow the existing unpaired experience.
- Long titles, multiline descriptions, large text, and English, Russian, and Kazakh copy.

Show action feedback without claiming an unsaved choice succeeded. Specify whether an error restores the previous card or keeps it visible for retry. Never label an unanswered or failed choice as a partner rejection.

## Responsive and accessible behavior

Deliver mobile, tablet portrait/landscape, and desktop/web layouts following existing adaptive navigation. Keep the deck comfortably bounded on wide screens; do not stretch one card across the full viewport.

Define keyboard focus order and visible focus states. Buttons must fully replace gestures; left/right arrow shortcuts may work while the deck is focused, but must not interfere with text entry or other controls. Provide accessible action names, readable contrast, sufficient touch targets, and feedback that does not rely on color alone. Reduced motion must preserve all functionality.

## Deliverables and acceptance criteria

- Editable Figma frames for the complete flow and required states, with light/dark coverage and responsive variants.
- Reused design-system components and documented variants for new deck cards and choice controls.
- A prototype covering both swipe directions, undo, mutual interest, and conversion through the existing form.
- Handoff annotations for drag thresholds, cancellation, transitions, keyboard behavior, focus restoration, and error recovery.
- A copy inventory with localization notes, consistently using the final feature name Wish Sparks.
- Clear separation between personal interest, mutual suggestions, and saved wishes in every screen.

Evaluate whether users understand that liking does not create a wish, can find discovery from an empty list, and can complete the flow without swiping. Product success should focus on ideas becoming wishes and eventually memories, rather than swipe volume.
