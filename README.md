# Stringee Flutter

A Flutter plugin for Android and iOS that provides access to Stringee API services.
It can be integrated into Flutter applications that need Stringee communication features.

## Features

* Voice call
* Video call
* Chat 1-1, chat group
* Video conference

## Getting Started

Check out our comprehensive [Example](https://github.com/stringeecom/flutter-samples) provided with
this plugin.

To use this package, add the dependency to your pubspec.yaml file.

```yaml
dependencies:
  flutter:
    sdk: flutter
  stringee_plugin: ^1.3.2
```

See the available versions and release notes in the [Changelog](CHANGELOG.md).

### Android requirements

- Android `minSdk` 21 or later
- Android SDK Platform 36 installed

The plugin is compiled with Android API 36. If your application declares its own `compileSdk`, set
it to 36 or later:

```gradle
android {
    compileSdk = 36
}
```

Check out our getting-started guides:

- [Getting started with Stringee Call API using Flutter Plugin](https://asia-1.console.stringee.com/docs/getting-started-flutter)
- [Getting started with Stringee Call2 API using Flutter Plugin](https://developer.stringee.com/docs/getting-started/getting-started-flutter2)
- [Getting started with Stringee Chat API using Flutter Plugin](https://developer.stringee.com/docs/flutter-plugin/install)
- [Getting started with Stringee Video Conference API using Flutter Plugin](https://developer.stringee.com/docs/video-conference-get-started-flutter)

## License

This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for
details.
