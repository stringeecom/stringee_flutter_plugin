
# Changelog

## 1.3.2 (unreleased)

- Prevent Android video views from crashing when a call renderer is temporarily unavailable
- Preserve pending video render options so Call and Call2 streams can attach after track changes

## 1.3.1

- Upgrade Stringee Android SDK to 2.1.13
- Upgrade Android WebRTC SDK to 144.7559.09
- Compile the Android plugin with Android API 36
- Add support for checking whether a call exists
- Improve Flutter payload parsing for native SDK responses

## 1.3.0

- Update core android sdk and ios sdk

## 1.2.20

- Bugfix: Fix issue crash on Android tablet

## 1.2.19

- Upgrade android sdk version
- Allow to set trust all certificate in android

## 1.2.18

- Bugfix: Fix issue crash on some video call in iOS

## 1.2.17

- Update iOS WebRTC version
- Add func sendCallInfo to StringeeCall2 on iOS

## 1.2.16

- Update Android WebRTC version

## 1.2.15

- Bugfix: Crash on some iOS devices

## 1.2.14

- Feature: Separate the audio device management feature from the call feature.

## 1.2.13

- Bugfix: Update file name podspec with new plugin name.

## 1.2.12

- Improved:
  - Call feature
  - Upgrade android native sdk and ios native sdk
- Bugfix:
  - Return null value when calling disconnect function

## 1.2.11

- Add iOS Call2 video-track events and rendering support
- Fix conversation user parsing on Android
- Upgrade the Android SDK to 2.1.1 and the iOS SDK to 1.9.20

## 1.2.10

- Fix conversation user role serialization
- Disable unsupported screen-capture track creation

## 1.2.9

- Fix connectivity on Dual-SIM iPhones

## 1.2.8

- Fix a crash when checking whether a call exists

## 1.2.7

- Bugfix: iOS voIP socket will not wake on iOS 16

## 1.2.6

- Improved: Upgrade android webrtc version

## 1.2.5

- Improved: Upgrade android native sdk

## 1.2.4

- Bugfix: Crash when convert message from android native

## 1.2.3

- Feature: Allow to update phone number when updating user info

## 1.2.2

- Feature: Allow to remove device token by package name

## 1.2.1

- Bugfix: Fix StringeeVideoView in android

## 1.2.0

- Feature: video conference in android
- Feature: share screen in StringeeCall2 in android

## 1.1.0

- Improved: Upgrade sdk

## 1.0.0

- Feature: live chat
