class StringeeServerAddress {
  String? _host;
  int? _port;

  String? get host => _host;

  int? get port => _port;

  @override
  String toString() {
    return {
      if (_host != null) 'host': _host!.trim(),
      if (_port != null) 'port': _port,
    }.toString();
  }

  StringeeServerAddress(String host, int port) {
    this._host = host;
    this._port = port;
  }

  Map<String, dynamic> toJson() {
    return {
      if (_host != null) 'host': _host!.trim(),
      if (_port != null) 'port': _port,
    };
  }
}
