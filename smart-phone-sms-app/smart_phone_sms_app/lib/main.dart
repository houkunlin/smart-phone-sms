import 'package:flutter/material.dart';
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

import 'MyHomePage.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '短信系统',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: '短信系统1'),
    );
  }
}

// StompClient client = StompClient(
//     config: StompConfig.SockJS(
//         url: 'http://127.0.0.1:8888/ws/stomp',
//         onConnect: (StompClient client, StompFrame connectFrame) {
//           // use the client object passed.
//         }));
// client.activate();
// client.subscribe(
//     destination: '/ws/subscribe/users',
//     headers: {},
//     callback: (frame) {
//       // Received a frame for this subscription
//       print(frame.body);
//     });
// client.send(destination: '/hello1', body: '{"name":"test"}', headers: {});
