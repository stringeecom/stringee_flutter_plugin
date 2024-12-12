class Result {
  final bool status;
  final int code;
  final String message;
  final dynamic data;

  Result({
    required this.status,
    required this.code,
    required this.message,
    this.data,
  });

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
