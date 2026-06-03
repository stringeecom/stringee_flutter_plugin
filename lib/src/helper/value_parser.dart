class StringeeValueParser {
  static String? toStringValue(dynamic value) {
    if (value == null) return null;
    if (value is String) return value;
    return value.toString();
  }

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

  static double? toDouble(dynamic value) {
    if (value == null) return null;
    if (value is double) return value;
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value);
    return null;
  }

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

  static Map<dynamic, dynamic>? toMap(dynamic value) {
    if (value is Map) return Map<dynamic, dynamic>.from(value);
    return null;
  }

  static List<dynamic> toList(dynamic value) {
    if (value is List) return value;
    return const [];
  }

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
