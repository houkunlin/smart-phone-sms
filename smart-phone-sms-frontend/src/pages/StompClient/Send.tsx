import React from 'react';
import { Headers } from '@/pages/StompClient/Headers';
import { Button, Form, Input, message } from 'antd';
// import { connect } from "dva";
import { connect } from 'umi';

class Send extends React.Component<any, any> {

  onSend = (values: any) => {
    const { client } = this.props;
    if (client == null) {
      message.error('未连接服务器');
      return;
    }
    console.log('发送', values);
    client.send(values.uri, {}, values.content);
  };

  render() {
    return (
      <div>
        <Form onFinish={this.onSend} initialValues={{ uri: '', content: '' }}>
          <Form.Item label="地址" name="uri">
            <Input placeholder="请输入发送URI" allowClear />
          </Form.Item>
          <Form.Item label="内容" name="content">
            <Input.TextArea placeholder="请输入发送内容" rows={6} allowClear />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">发送</Button>
          </Form.Item>
        </Form>
        <Headers />
      </div>
    );
  }
}

export default connect(({ Connect: ConnectState, loading }: any) => ({
  client: ConnectState.client, loading,
}), null, null, { forwardRef: true })(Send);
