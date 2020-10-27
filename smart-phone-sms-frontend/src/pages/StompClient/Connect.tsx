import React from 'react';
import { Button, Form, Input, message, Spin, Tabs } from 'antd';
import { Headers } from '@/pages/StompClient/Headers';
import { FormInstance } from 'antd/lib/form';
import Stomp, { Frame } from 'stompjs';
import { connect } from 'umi';
import { ConnectProps } from '@@/plugin-dva/connect';
import SockJS from 'sockjs-client';

interface Props extends ConnectProps {
  [key: string]: any
}

class Connect extends React.Component<Props, any> {
  formRef = React.createRef<FormInstance>();

  headerRef = React.createRef<Headers>();

  setPropsState = (payload: any) => {
    this.props.dispatch!!({ type: 'Connect/setState', payload });
  };

  onConnect = (values: any) => {
    const { url = '' } = values;
    const headers: any = this.headerRef.current?.result() || {};
    const account = this.formRef.current?.getFieldsValue() || {};
    console.log('连接服务器', values, headers, account);
    this.setPropsState({ connecting: true, client: null });
    // 使用 STOMP 子协议的 WebSocket 客户端
    const client = url.startsWith('ws') ? Stomp.over(new WebSocket(url)) : Stomp.over(new SockJS(url));
    client.debug = () => {
    };

    const successFun = (frame?: Frame) => {
      console.log('连接成功', frame);
      this.setPropsState({ connecting: false, client });
    };
    const errorFun = (error: Frame | string) => {
      console.log('连接失败', error);
      this.setPropsState({ connecting: false, client: null });
      message.error(`连接服务器失败：${error}`);
    };
    if (account.username && account.username.trim().length > 0) {
      client.connect(account.username, account.password, successFun, errorFun);
    } else if (headers.login && headers.login.trim().length > 0) {
      client.connect(headers, successFun, errorFun);
    } else {
      client.connect(headers, successFun, errorFun);
    }
  };

  onDisconnect = () => {
    this.setPropsState({ connecting: true });
    const { client } = this.props.state;
    client?.disconnect(() => {
      this.setPropsState({ connecting: false, client: null });
    });
  };

  render() {
    const { client, connecting } = this.props.state;
    console.log('this.props', this.props);
    return (
      <Spin spinning={connecting}>
        <Form
          layout="inline"
          onFinish={this.onConnect}
          initialValues={{ url: '' }}
        >
          <Form.Item
            label="URL"
            name="url"
            rules={[{ required: true, message: '请输入连接地址!' }]}
            style={{ width: '500px' }}
          >
            <Input placeholder="请输入链接地址" style={{ width: '100%' }} disabled={client != null} />
          </Form.Item>
          <Form.Item>
            {client == null && (
              <Button type="primary" htmlType="submit">
                链接
              </Button>
            )}
            {client != null && (
              <Button type="primary" htmlType="button" danger onClick={this.onDisconnect}>
                断开连接
              </Button>
            )}</Form.Item>
        </Form>
        <Tabs>
          <Tabs.TabPane key="password" tab="Password">
            <Form ref={this.formRef} layout="inline">
              <Form.Item label="帐号" name="username">
                <Input placeholder="请输入帐号" allowClear />
              </Form.Item>
              <Form.Item label="密码" name="password">
                <Input.Password placeholder="请输入密码" allowClear />
              </Form.Item>
            </Form>
          </Tabs.TabPane>
          <Tabs.TabPane key="header" tab="Header">
            <Headers ref={this.headerRef} />
          </Tabs.TabPane>
        </Tabs>
      </Spin>
    );
  }
}

export default connect(({ Connect: state, loading }: any) => ({
  state, loading,
}), null, null, { forwardRef: true })(Connect);
