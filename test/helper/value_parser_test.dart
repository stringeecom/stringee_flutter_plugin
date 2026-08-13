import 'package:flutter_test/flutter_test.dart';
import 'package:stringee_plugin/src/helper/value_parser.dart';

enum _State { first, second }

void main() {
  group('StringeeValueParser', () {
    test('converts primitive values without throwing', () {
      expect(StringeeValueParser.toStringValue(42), '42');
      expect(StringeeValueParser.toStringValue(null), isNull);

      expect(StringeeValueParser.toInt(12.8), 12);
      expect(StringeeValueParser.toInt('12.8'), 12);
      expect(StringeeValueParser.toInt(true), 1);
      expect(StringeeValueParser.toInt('invalid'), isNull);

      expect(StringeeValueParser.toDouble(12), 12.0);
      expect(StringeeValueParser.toDouble('12.5'), 12.5);
      expect(StringeeValueParser.toDouble('invalid'), isNull);
    });

    test('parses supported boolean representations', () {
      expect(StringeeValueParser.toBool(true), isTrue);
      expect(StringeeValueParser.toBool(1), isTrue);
      expect(StringeeValueParser.toBool(0), isFalse);
      expect(StringeeValueParser.toBool(' TRUE '), isTrue);
      expect(StringeeValueParser.toBool('0'), isFalse);
      expect(StringeeValueParser.toBool('invalid'), isNull);
    });

    test('copies maps and safely falls back for unsupported collections', () {
      final source = <String, dynamic>{'id': 1};
      final parsed = StringeeValueParser.toMap(source)!;

      expect(parsed, source);
      expect(identical(parsed, source), isFalse);
      expect(StringeeValueParser.toMap('not a map'), isNull);
      expect(StringeeValueParser.toList(<int>[1, 2]), <int>[1, 2]);
      expect(StringeeValueParser.toList('not a list'), isEmpty);
    });

    test('selects enum values with a fallback for invalid indexes', () {
      expect(
        StringeeValueParser.enumValue(_State.values, 1, _State.first),
        _State.second,
      );
      expect(
        StringeeValueParser.enumValue(_State.values, 9, _State.first),
        _State.first,
      );
      expect(
        StringeeValueParser.enumValue(_State.values, 'invalid', _State.second),
        _State.second,
      );
    });
  });
}
