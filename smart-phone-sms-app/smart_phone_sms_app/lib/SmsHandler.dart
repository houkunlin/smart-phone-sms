import 'package:flutter/services.dart';

class SmsHandler {
  static final MethodChannel _channel = MethodChannel('com.houkunlin/SmsManager');

  static Future<T> sendSMS<T>(String phone, String msg) {
    if (phone == null || phone.trim().isEmpty || msg == null || msg.trim().isEmpty) {
      return Future.error("请输入手机号和短信内容");
    }
    var args = List();
    args.add(phone.trim());
    args.add(null);
    args.add(msg.trim());
    args.add(null);
    args.add(null);

    return _channel.invokeMethod('sendTextMessage', args);
  }
}
