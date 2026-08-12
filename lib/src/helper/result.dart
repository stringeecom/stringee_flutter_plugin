/// A normalized result returned by Stringee platform-channel operations.
class Result {
  /// Whether the operation succeeded.
  final bool status;

  /// Native Stringee or plugin error code.
  final int code;

  /// Human-readable result message.
  final String message;

  /// Optional operation-specific payload.
  final dynamic data;

  /// Creates a result value.
  Result({
    required this.status,
    required this.code,
    required this.message,
    this.data,
  });

  /// Parses a result from a native channel [json] payload.
  static Result fromJson(Map<dynamic, dynamic> json) {
    return Result(
      status: json['status'],
      code: json['code'],
      message: json['message'],
      data: json['body'],
    );
  }

  @override
  String toString() {
    return 'Result{status: $status, code: $code, message: $message, data: $data}';
  }
}
