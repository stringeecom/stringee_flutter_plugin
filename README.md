# Stringee Flutter

A Dart package for all platforms which helps developers with Stringee API services.
This Dart Package can be integrated into any Flutter application to make use of Stringee API.

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
  stringee_plugin: $version
```

Replace `$version` with the plugin version you want to use. See a list of versions in [Changelog](CHANGELOG.md).

## iOS dependency managers

This plugin supports both CocoaPods and Swift Package Manager on iOS.

CocoaPods remains the default path for older Flutter projects. It intentionally stays on the native `Stringee` CocoaPods distribution declared in `ios/stringee_plugin.podspec` (`~> 2.0.2`).

Swift Package Manager is available for Flutter SDKs that support Flutter plugin SPM integration. To opt in:

```sh
flutter config --enable-swift-package-manager
```

In SPM mode, the plugin resolves `Stringee-iOS-SDK-SPM` exact `2.0.3`. That package also resolves its native WebRTC dependency.

Check out our getting started guide at here:

- [Getting started with Stringee Call API using Flutter Plugin](https://asia-1.console.stringee.com/docs/getting-started-flutter)
- [Getting started with Stringee Call2 API using Flutter Plugin](https://developer.stringee.com/docs/getting-started/getting-started-flutter2)
- [Getting started with Stringee Chat API using Flutter Plugin](https://developer.stringee.com/docs/flutter-plugin/install)
- [Getting started with Stringee Video Conference API using Flutter Plugin](https://developer.stringee.com/docs/video-conference-get-started-flutter)

## License

This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for
details.
