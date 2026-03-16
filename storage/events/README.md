File-backed event storage lives here at runtime.

- `events.jsonl` is created automatically by the app.
- Records include session ID, page slug, event type, referrer, and lightweight payload data such as draft ID, route, and step.
- This directory is intended for simple launch-stage attribution and behavior checks.
