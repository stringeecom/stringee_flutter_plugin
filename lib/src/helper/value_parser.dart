/// Safe conversions for loosely typed platform-channel values.
class StringeeValueParser {
  /// Returns [value] as a string, or `null` when it is absent.
  static String? toStringValue(dynamic value) {
    if (value == null) return null;
    if (value is String) return value;
    return value.toString();
  }

  /// Parses [value] as an integer without throwing.
  static int? toInt(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    if (value is num) return value.toInt();
    if (value is String) {
      return int.tryParse(value) ?? double.tryParse(value)?.toInt();
    }
    if (value is bool) return value ? 1 : 0;
    return null;
  }

  /// Parses [value] as a double without throwing.
  static double? toDouble(dynamic value) {
    if (value == null) return null;
    if (value is double) return value;
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value);
    return null;
  }

  /// Parses common native boolean representations.
  static bool? toBool(dynamic value) {
    if (value == null) return null;
    if (value is bool) return value;
    if (value is num) return value != 0;
    if (value is String) {
      final normalized = value.trim().toLowerCase();
      if (normalized == 'true' || normalized == '1') return true;
      if (normalized == 'false' || normalized == '0') return false;
    }
    return null;
  }

  /// Returns a map copy of [value], or `null` when it is not a map.
  static Map<dynamic, dynamic>? toMap(dynamic value) {
    if (value is Map) return Map<dynamic, dynamic>.from(value);
    return null;
  }

  /// Returns [value] as a list, or an empty list when it is not a list.
  static List<dynamic> toList(dynamic value) {
    if (value is List) return value;
    return const [];
  }

  /// Selects an enum entry by native [index], returning [fallback] if invalid.
  static T enumValue<T>(List<T> values, dynamic index, T fallback) {
    final parsedIndex = toInt(index);
    if (parsedIndex == null ||
        parsedIndex < 0 ||
        parsedIndex >= values.length) {
      return fallback;
    }
    return values[parsedIndex];
  }
}
