# ExternalControllerKit_Android

Android SDK and demo app for mapping external hardware input (game controllers, keyboards, MIDI) to host-defined actions.

## Modules

- `externalcontrollerkit` — core models, providers, persistence, logging, and `ExternalController`
- `externalcontrollerkitui` — Compose configuration UI and `ExternalControllerViewModel`
- `app` — runnable Hilt demo app

## Demo flow

1. Launch the demo app.
2. Open the External Controller screen.
3. Select a connected device.
4. Tap an action and press a hardware input.
5. Return to the home screen and trigger mapped actions.

## Notes

- Mappings are persisted as JSON with DataStore.
- Runtime events are exposed with `StateFlow` / `SharedFlow`.
- `MainActivity` forwards `KeyEvent` and `MotionEvent` instances into the SDK.
