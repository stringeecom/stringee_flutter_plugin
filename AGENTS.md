# Repository Guidelines

## Project Structure & Module Organization

This repository is a Flutter plugin for Stringee voice, video, chat, and conferencing.

- `lib/stringee_plugin.dart` is the public Dart export.
- `lib/src/` contains Dart wrappers for client, call, chat, audio, video view, and conference APIs.
- `android/src/main/java/com/stringee/stringeeflutterplugin/` contains the Android native plugin bridge.
- `ios/Classes/` contains the iOS native plugin bridge.
- `example/` is the runnable Flutter sample app; its tests live in `example/test/`.
- Platform assets are under `example/android/app/src/main/res/` and `example/ios/Runner/Assets.xcassets/`.

## Build, Test, and Development Commands

Use Flutter through FVM on this machine:

```powershell
& 'F:\fvm\default\bin\flutter.bat' pub get
& 'F:\fvm\default\bin\flutter.bat' analyze
& 'F:\fvm\default\bin\flutter.bat' test example/test
& 'F:\fvm\default\bin\flutter.bat' run -d <device-id> -t example/lib/main.dart
```

`pub get` resolves Dart dependencies, `analyze` checks Dart lints and type issues, `test` runs Flutter widget/unit tests, and `run` launches the example app on an emulator or device. For Android native-only checks, use `android/gradlew.bat` from the `android/` directory.

## Coding Style & Naming Conventions

Format Dart with `dart format` or the Flutter formatter before submitting changes. Follow `flutter_lints` where configured in `example/analysis_options.yaml`. Keep Dart files in `snake_case.dart` when adding new files, classes in `UpperCamelCase`, and methods/fields in `lowerCamelCase`.

Existing public API files include names such as `StringeeClient.dart`; keep existing names stable unless intentionally making a breaking API change. Match native platform style: Java classes in `UpperCamelCase`, Objective-C files as paired `.h`/`.m`, and channel/event constants grouped near existing managers.

## Testing Guidelines

Add focused tests under `example/test/` for Flutter-facing behavior. Name test files with the `_test.dart` suffix, for example `widget_test.dart`. Run:

```powershell
& 'F:\fvm\default\bin\flutter.bat' test example/test
```

For native bridge changes, also validate the affected call, chat, audio, or conference flow in the example app on the target platform. Document any manual device coverage in the pull request.

## Commit & Pull Request Guidelines

Recent history uses short imperative or conventional-style subjects, for example `feat: Bump version to 1.3.0 and update native SDKs` and `Update Stringee SDK to version 2.1.8 in build.gradle`. Keep commits scoped and mention issue keys when available, such as `SPT-1140`.

Pull requests should include a concise summary, linked issue or ticket, test results, and platform coverage. Include screenshots or short recordings for visible example-app UI changes.

## Security & Configuration Tips

Do not commit real access tokens, customer identifiers, provisioning profiles, or local signing credentials. Keep secrets in local environment configuration and use placeholder values in sample code.
