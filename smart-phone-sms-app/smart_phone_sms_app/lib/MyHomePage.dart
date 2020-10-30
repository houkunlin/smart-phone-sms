import 'dart:convert';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:smart_phone_sms_app/SmsHandler.dart';
import 'package:stomp_dart_client/sock_js/sock_js_utils.dart';
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  /*
  此小部件是您的应用程序的主页。
  它是有状态的，表示它具有一个State对象（定义如下），
  该对象包含影响其外观的字段。此类是状态的配置。
  它保存由父级（在此例中为App小部件）提供并由State的build方法使用的值（在本例中为标题）。
  Widget子类中的字段始终标记为“最终”。
  、*/
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> with SingleTickerProviderStateMixin {
  Future<String> _askedToLead() async {
    final TextEditingController _controller = new TextEditingController();
    return await showDialog<String>(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('弹幕内容'),
            content: SingleChildScrollView(
              child: ListBody(
                children: [
                  TextField(
                    controller: _controller,
                    obscureText: false,
                    decoration:
                        InputDecoration(border: OutlineInputBorder(), labelText: '弹幕内容', hintText: '请输入弹幕内容 ...'),
                  )
                ],
              ),
            ),
            actions: [
              FlatButton(
                child: Text('确定'),
                onPressed: () {
                  Navigator.of(context).pop(_controller.text);
                },
              ),
            ],
          );
        });
  }

  GlobalKey _formKey = new GlobalKey<FormState>();
  final TextEditingController url = new TextEditingController();
  final TextEditingController login = new TextEditingController();
  final TextEditingController passcode = new TextEditingController();
  final JsonDecoder json = JsonDecoder();

  void _requestPermission() async {
    Map<PermissionGroup, PermissionStatus> permissions = await PermissionHandler().requestPermissions(
        [PermissionGroup.storage, PermissionGroup.sms, PermissionGroup.microphone, PermissionGroup.phone]);
    print('权限申请结果：$permissions');
  }

  void send() {
    // MethodChannel('com.houkunlin/SubscriptionManager')
    //     .invokeMethod('getActiveSubscriptionInfoList')
    //     .then((value) => {print('测试方法：$value')})
    //     .catchError((onError) {
    //   print('测试方法失败: $onError');
    // });
    var args = List();
    args.add("+8615577405667");
    args.add(null);
    args.add("这里是通过Flutter发送的内容");
    args.add(null);
    args.add(null);

    // MethodChannel('com.houkunlin/SmsManager')
    //     .invokeMethod('sendTextMessage', args)
    //     .then((value) => {print('测试方法：$value')})
    //     .catchError((onError) {
    //   print('测试方法失败: $onError');
    // });
  }

  void loginAction() {
    // 通过_formKey.currentState 获取FormState后，调用validate()方法校验用户名密码是否合法，校验通过后再提交数据。
    FormState state = _formKey.currentState as FormState;
    print("_formKey.currentState: $state");
    print("_formKey.values: ${url.text}, ${login.text}:${passcode.text}");
    if (state.validate()) {
      //验证通过提交数据
      var isSockJs = url.text.startsWith("http");
      StompClient client = StompClient(
        config: StompConfig(
          url: isSockJs ? SockJsUtils().generateTransportUrl(url.text) : url.text,
          useSockJS: isSockJs,
          stompConnectHeaders: {"login": login.text, "passcode": passcode.text},
          onConnect: (StompClient client, StompFrame connectFrame) {
            // use the client object passed.
            print("连接成功：$connectFrame}");
            client.subscribe(
                destination: '/ws/subscribe/sms',
                callback: (frame) {
                  print("收到短信：${frame.body}");
                  Map result = json.convert(frame.body);
                  SmsHandler.sendSMS(result["phone"], result["msg"]);
                });
          },
          onStompError: (StompFrame frame) {
            print("连接失败：$frame");
          },
          onDisconnect: (StompFrame frame) {
            print("关闭连接：$frame");
          },
          onDebugMessage: (String msg) {
            print("调试：$msg");
          },
        ),
      );
      client.activate();
      print("连接Stomp");
      // client.send(destination: '/hello1', body: '{"name":"test"}', headers: {});
    }
  }

  @override
  Widget build(BuildContext context) {
    _requestPermission();

    return Scaffold(
      drawer: Drawer(
        child: ListView(padding: EdgeInsets.all(5.0), children: [
          DrawerHeader(
            decoration: BoxDecoration(
              color: Colors.blue,
            ),
            child: Text(
              '弹幕',
              style: TextStyle(
                color: Colors.white,
                fontSize: 24,
              ),
            ),
          ),
          ListTile(
            title: Text('弹幕内容'),
            leading: Icon(Icons.message),
            onTap: () {
              Future<String> result = _askedToLead();
              result.then((value) => print('result:$value'));
            },
          ),
        ]),
      ),
      appBar: AppBar(
        title: Text("短信系统"),
      ),
      body: Container(
        // 中心是一个布局小部件。它需要一个孩子并将其放置在父母中间。
        alignment: Alignment.center,
        padding: EdgeInsets.only(),
        child: Column(
          children: [
            Form(
              key: _formKey,
              autovalidateMode: AutovalidateMode.always,
              child: Column(
                children: [
                  TextFormField(
                    controller: url,
                    keyboardType: TextInputType.text,
                    decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      labelText: 'URL',
                      hintText: '请输入连接URL',
                    ),
                    validator: (v) {
                      return v.trim().isEmpty ? "URL不能为空" : null;
                    },
                  ),
                  TextFormField(
                    controller: login,
                    keyboardType: TextInputType.text,
                    decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      labelText: '用户名',
                      hintText: '请输入登录帐号',
                    ),
                  ),
                  TextFormField(
                    controller: passcode,
                    keyboardType: TextInputType.text,
                    obscureText: false,
                    decoration: InputDecoration(
                      border: UnderlineInputBorder(),
                      labelText: '密码',
                      hintText: '请输入登录密码',
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.only(top: 28.0),
                    child: Row(
                      children: [
                        Expanded(
                          child: RaisedButton(
                            padding: EdgeInsets.all(15.0),
                            child: Text("登录"),
                            color: Theme.of(context).primaryColor,
                            textColor: Colors.white,
                            onPressed: this.loginAction,
                          ),
                        ),
                      ],
                    ),
                  )
                ],
              ),
            ),
            Text(url.text),
            Text(login.text),
            Text(passcode.text),
            RaisedButton(
              padding: EdgeInsets.all(15.0),
              child: Text("发送短信"),
              color: Theme.of(context).primaryColor,
              textColor: Colors.white,
              onPressed: this.send,
            ),
          ],
        ),
        // color: Color.fromRGBO(240, 240, 240, 1.0),
        decoration: BoxDecoration(),
        // height: 640,
        // width: 360,
      ),
      backgroundColor: Colors.white,
    );
  }
}
