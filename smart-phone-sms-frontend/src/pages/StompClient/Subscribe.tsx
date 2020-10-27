import React from 'react';
import { Button, Checkbox, Form, Input, message } from 'antd';
import { Client } from 'stompjs';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { docco } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { FormInstance } from 'antd/lib/form';
import { connect } from 'umi';

export interface SubscribeProps {
  client: Client | undefined
}

interface State {
  subscribeId: string | null
  result: string
  openLongLog: boolean
}

export class SubscribeInstance extends React.Component<SubscribeProps, State> {
  formRef = React.createRef<FormInstance>();

  state: State = {
    subscribeId: null,
    result: '',
    openLongLog: false,
  };

  onSubscribe = (values: any) => {
    console.log('订阅数据', values);
    const { client } = this.props;
    if (client == null) {
      message.error('未连接服务器');
      return;
    }
    const subscribe = client.subscribe(values.uri, (msg) => {
      console.log('接收数据', values.uri, msg);
      const { openLongLog } = this.state;
      let { result } = this.state;
      if (openLongLog) {
        result += msg.body;
      } else if (msg.headers['content-type'] === 'application/json') {
        result = JSON.stringify(JSON.parse(msg.body), null, 2);
      } else {
        result = msg.body;
      }
      result += '\r\n';
      this.setState({ result });
    });
    console.log('订阅数据', subscribe);
    this.setState({ subscribeId: subscribe.id });
  };

  onUnsubscribe = () => {
    const { client } = this.props;
    const { subscribeId } = this.state;
    if (client == null) {
      message.error('未连接服务器');
      return;
    }
    console.log('取消订阅', subscribeId);
    this.setState({ subscribeId: null }, () => {
      client.unsubscribe(subscribeId!!);
    });
  };

  onValuesChange = (changedValues: any, allValues: any) => {
    if (changedValues.longLog != null) {
      this.setState({ openLongLog: changedValues.longLog });
    }
  };

  isSubscribe = () => {
    const { client } = this.props;
    const { subscribeId } = this.state;
    if (client == null) {
      if (subscribeId != null) {
        this.setState({ subscribeId: null });
      }
      return false;
    }
    return subscribeId != null;
  };

  destroy = () => {
    const { client } = this.props;
    const { subscribeId } = this.state;
    if (client == null) {
      return;
    }
    console.log('销毁，取消订阅', subscribeId);
    this.setState({ subscribeId: null }, () => {
      client.unsubscribe(subscribeId!!);
    });
  };

  render() {
    const { result, openLongLog } = this.state;
    const isSubscribe = this.isSubscribe();
    console.log('', this, this.props);
    return (
      <div>
        <Form
          ref={this.formRef}
          layout="inline"
          onFinish={this.onSubscribe}
          onValuesChange={this.onValuesChange}
          initialValues={{ uri: '', longLog: openLongLog }}
          style={{ marginBottom: 20 }}
        >
          <Form.Item label="地址" name="uri">
            <Input placeholder="请输入订阅URI" allowClear disabled={isSubscribe} />
          </Form.Item>
          <Form.Item>
            {!isSubscribe && (
              <Button type="primary" htmlType="submit">订阅</Button>
            )}
            {isSubscribe && (
              <Button danger onClick={this.onUnsubscribe}>取消订阅</Button>
            )}
          </Form.Item>
          <Form.Item name="longLog" valuePropName="checked">
            <Checkbox>持续记录</Checkbox>
          </Form.Item>
        </Form>
        <SyntaxHighlighter language="javascript" style={docco}>
          {result}
        </SyntaxHighlighter>
      </div>
    );
  }
}

export default connect(({ Connect: ConnectState, loading }: any) => ({
  client: ConnectState.client, loading,
}), null, null, { forwardRef: true })(SubscribeInstance);
