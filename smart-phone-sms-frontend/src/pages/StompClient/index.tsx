import React from 'react';
import { Card, Col, Dropdown, Menu, Row, Tabs } from 'antd';
import Connect from '@/pages/StompClient/Connect';
import './index.less';
import { v4 as uuid } from 'uuid';
import { FormInstance } from 'antd/lib/form';
import { PageContainer } from '@ant-design/pro-layout';
import { connect } from 'umi';
import Send from './Send';
import Subscribe, { SubscribeInstance } from './Subscribe';
import { DownOutlined, UserOutlined } from '@ant-design/icons';

const { TabPane } = Tabs;

export interface PageTabPane {
  key: string
  title: string
  closable: boolean
}

interface State {
  /**
   * 发送者列表
   */
  sendTabs: PageTabPane[]
  sendActiveKey: string
  /**
   * 订阅者列表
   */
  subscribeTabs: PageTabPane[]
  subscribeKey: string
}

const defaultKey = uuid();

class StompClient extends React.Component<any, State> {
  formRef: React.RefObject<FormInstance> = React.createRef<FormInstance>();

  subscribeRefs: React.RefObject<SubscribeInstance>[] = [React.createRef<SubscribeInstance>()];


  state: State = {
    sendTabs: [{
      key: defaultKey,
      title: '默认',
      closable: false,
    }],
    sendActiveKey: defaultKey,
    subscribeTabs: [{
      key: defaultKey,
      title: '默认',
      closable: false,
    }],
    subscribeKey: defaultKey,
  };

  onEditSendTabs = (targetKey: any, action: string) => {
    console.log('onEditSendTabs', targetKey, action);
    const { sendTabs: tabs, sendActiveKey: activeKey } = this.state;
    if (action === 'add') {
      const key = uuid();
      const item: PageTabPane = {
        title: `Tab-${tabs.length}`,
        key,
        closable: tabs.length > 0,
      };
      tabs.push(item);
      this.setState({ sendTabs: tabs, sendActiveKey: key });
    } else if (action === 'remove') {
      const index = tabs.findIndex(pane => pane.key === targetKey);
      const newSendTabs = tabs.filter(pane => pane.key !== targetKey);
      let newActiveKey = activeKey;
      if (newSendTabs.length && activeKey === targetKey) {
        newActiveKey = newSendTabs[index >= 0 ? index - 1 : 0].key;
      }
      this.setState({ sendTabs: newSendTabs, sendActiveKey: newActiveKey });
    }
  };

  onEditSubscribeTabs = (targetKey: any, action: string) => {
    console.log('onEditSendTabs', targetKey, action);
    const { subscribeTabs: tabs, subscribeKey: activeKey } = this.state;
    if (action === 'add') {
      const key = uuid();
      this.subscribeRefs.push(React.createRef<SubscribeInstance>());
      const item: PageTabPane = {
        title: `Tab-${tabs.length}`,
        key,
        closable: tabs.length > 0,
      };
      tabs.push(item);
      this.setState({ subscribeTabs: tabs, subscribeKey: key });
    } else if (action === 'remove') {
      const index = tabs.findIndex(pane => pane.key === targetKey);
      this.subscribeRefs[index].current?.destroy();
      this.subscribeRefs.splice(index, 1);
      const newTabs = tabs.filter(pane => pane.key !== targetKey);
      let newActiveKey = activeKey;
      if (newTabs.length && activeKey === targetKey) {
        newActiveKey = newTabs[index >= 0 ? index - 1 : 0].key;
      }
      this.setState({ subscribeTabs: newTabs, subscribeKey: newActiveKey });
    }
  };

  render() {
    const { sendTabs, sendActiveKey, subscribeTabs, subscribeKey } = this.state;
    const { client } = this.props;
    const subscriptionKeys = client != null ? Object.keys(client.subscriptions) : [];
    const menu = (
      <Menu onClick={() => {
      }}>
        {subscriptionKeys.map(key => (
          <Menu.Item key={key} icon={<UserOutlined />}>
            {key}
          </Menu.Item>
        ))}
      </Menu>
    );
    let tabBarExtraContent = (<div />);
    if (subscriptionKeys.length > 0) {
      tabBarExtraContent = (
        <Dropdown overlay={menu} trigger={['click']}>
          <a className="ant-dropdown-link" onClick={e => e.preventDefault()}>
            所有订阅 <DownOutlined />
          </a>
        </Dropdown>
      );
    }
    return (
      <PageContainer>
        <Card>
          <Connect />
        </Card>
        <Row gutter={20}>
          <Col span={10}>
            <Card>
              <Tabs type="editable-card" onEdit={this.onEditSendTabs} activeKey={sendActiveKey}
                    onChange={(activeKey: string) => {
                      this.setState({ sendActiveKey: activeKey });
                    }}>
                {sendTabs.map((pane) => (
                  <TabPane tab={pane.title} key={pane.key} closable={pane.closable}>
                    <Send />
                  </TabPane>
                ))}
              </Tabs>
            </Card>
          </Col>
          <Col span={14}>
            <Card>
              <Tabs type="editable-card" onEdit={this.onEditSubscribeTabs} activeKey={subscribeKey}
                    onChange={(activeKey: string) => {
                      this.setState({ subscribeKey: activeKey });
                    }}
                    tabBarExtraContent={{
                      right: tabBarExtraContent,
                    }}
              >
                {subscribeTabs.map((pane, index) => (
                  <TabPane tab={pane.title} key={pane.key} closable={pane.closable}>
                    <Subscribe ref={this.subscribeRefs[index]} />
                  </TabPane>
                ))}
              </Tabs>
            </Card>
          </Col>
        </Row>
      </PageContainer>
    );
  }
}

export default connect(({ Connect: ConnectState, loading }: any) => ({
  client: ConnectState.client, loading,
}), null, null, { forwardRef: true })(StompClient);
