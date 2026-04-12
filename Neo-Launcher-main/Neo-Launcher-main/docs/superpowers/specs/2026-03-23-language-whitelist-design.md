# Language Whitelist Design

**Goal**

Restrict the app language picker to exactly one system-default option plus 19 explicit languages:
`en`, `de`, `el`, `es`, `fr`, `hi`, `hu`, `it`, `ja`, `ko`, `nl`, `pl`, `pt`, `ru`, `sv`, `th`, `tr`, `vi`, `zh-rTW`.

**Design**

The language picker currently derives entries from every translated `res/values-*` directory, which makes the set unstable and larger than the requested product scope. Replace that dynamic source with a fixed whitelist in the Omega preferences layer.

Keep the existing preference storage model:
- `""` means follow the system locale.
- A locale code like `de` or `zh-rTW` means force that app locale.

Generate labels with the existing locale summarization format so the UI still shows English plus the localized language name. Remove the extra synthetic "System -> en" entry so the picker has a single default concept.

**Impact**

- No resource files are deleted.
- No locale-application logic changes beyond the displayed choices.
- Existing saved values that are outside the new whitelist may remain stored, but new selections will be limited to the approved set.
