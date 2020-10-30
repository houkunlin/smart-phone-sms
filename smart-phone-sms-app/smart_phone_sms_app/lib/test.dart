import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

void main() {
  Map<String, String> map = <String, String>{};
  map.putIfAbsent("login", () => "侯坤林");
  map.putIfAbsent("passcode", () => "123456");

  StompClient client = StompClient(
    config: StompConfig.SockJS(
      url: "http://192.168.1.15:8888/ws/stomp",
      stompConnectHeaders: map,
      // beforeConnect: () {
      //   print("beforeConnect");
      // },
      onConnect: (StompClient client, StompFrame connectFrame) {
        // use the client object passed.
        print("连接成功：$connectFrame}");

        client.subscribe(
            destination: '/ws/subscribe/users',
            headers: {},
            callback: (frame) {
              // Received a frame for this subscription
              print(frame.body);
            });
        client.send(destination: '/hello1', body: '{"name":"test"}', headers: {});
      },
      onStompError: (StompFrame frame) {
        print("连接失败：$frame");
      },
      onDisconnect: (StompFrame frame) {
        print("关闭连接：$frame");
      },
      onWebSocketError: (dynamic msg) {
        print("onWebSocketError：$msg");
      },
      onWebSocketDone: () {
        print("onWebSocketDone");
      },
      onDebugMessage: (String msg) {
        print("调试：$msg");
      },
    ),
  );
  client.activate();
  print("连接Stomp");
}
