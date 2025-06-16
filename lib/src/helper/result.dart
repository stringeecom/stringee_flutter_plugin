import 'dart:convert';

import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

class Result extends Equatable {
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

  static Result fromJson(dynamic rawJson) {
    Map<dynamic, dynamic>? result;
    try {
      if (rawJson is Map<dynamic, dynamic>) {
        result = rawJson;
      } else if (rawJson is String) {
        result = json.decode(rawJson);
      }
    } catch (e) {
      debugPrint('Error parsing JSON: $e');
    }
    bool status = false;
    int code = -1;
    String message = 'Error parsing JSON';
    if (result != null) {
      status = result['status'] == true;
      code = result['code'] ?? -1;
      message = result['message'] ?? 'Error parsing JSON';
      result.remove('status');
      result.remove('code');
      result.remove('message');
    }
    return Result(
      status: status,
      code: code,
      message: message,
      data: result,
    );
  }

  Result copyWith({
    bool? status,
    int? code,
    String? message,
    dynamic data,
  }) {
    return Result(
      status: status ?? this.status,
      code: code ?? this.code,
      message: message ?? this.message,
      data: data ?? this.data,
    );
  }

  @override
  List<Object?> get props => [status, code, message, data];
}
