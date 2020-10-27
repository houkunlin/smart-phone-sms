import React from "react";
import { Col, Form, Input, Row } from "antd";
import { ClearOutlined, DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { FormInstance } from "antd/lib/form";
import './index.less'

interface KeyValue {
  key: string
  value: string
}

interface State {
  values: KeyValue[]

  [key: string]: any
}

export class Headers extends React.Component<any, State> {
  formRef = React.createRef<FormInstance>();

  state: State = {
    values: [{ key: '', value: '' }]
  }

  addItem = () => {
    const values = this.form2state()
    values.push({ key: '', value: '' })
    this.state2form(values)
    this.setState({ values })
  }

  deleteItem = (index: number) => {
    const values = this.form2state()
    values.splice(index, 1);
    this.state2form(values)
    this.setState({ values })
  }

  state2form = (values = this.state.values) => {
    const formValues = {}
    for (let index = 0; index < values.length; index++) {
      formValues[`key-${index}`] = values[index].key
      formValues[`value-${index}`] = values[index].value
    }
    this.formRef.current?.setFieldsValue(formValues)
    return formValues
  }

  form2state = (formValues = this.formRef.current?.getFieldsValue() || {}) => {
    const { values } = this.state;
    for (let index = 0; index < values.length; index++) {
      values[index].key = formValues[`key-${index}`]
      values[index].value = formValues[`value-${index}`]
    }
    this.setState({ values })
    return values
  }

  result = (values = this.form2state()) => {
    const result = {}
    for (let index = 0; index < values.length; index++) {
      const key = values[index].key;
      if (key && key.trim().length > 0) {
        result[key] = values[index].value;
      }
    }
    return result
  }

  render() {
    const { values } = this.state;
    const initValues = this.state2form(values)
    // console.log('initValues', initValues, 'FieldsValue', this.formRef.current?.getFieldsValue())
    const iconWidth = 24 * 3 - 10
    return (
      <Form ref={this.formRef} initialValues={initValues} style={{marginBottom: 0}}>
        {values.map((value, index) => (
          <Row gutter={10} key={`index-${index}`}>
            <Col span={4}>
              <Form.Item name={`key-${index}`}>
                <Input placeholder="Key" allowClear />
              </Form.Item>
            </Col>
            <Col span={20}>
              <div style={{ float: 'left', width: `calc(100% - ${iconWidth}px - 10px)` }}>
                <Form.Item name={`value-${index}`}>
                  <Input placeholder="Value" allowClear />
                </Form.Item>
              </div>
              <div style={{ float: 'left', width: iconWidth, marginLeft: 10 }}>
                <Form.Item style={{ width: iconWidth }}>
                  <ClearOutlined className="icon-item icon-pointer" onClick={() => {
                    const result = {};
                    result[`key-${index}`] = '';
                    result[`value-${index}`] = '';
                    this.formRef.current?.setFieldsValue(result)
                  }} />
                  {(values.length == 1) && (
                    <DeleteOutlined className="icon-item" style={{ color: 'rgba(180,180,180,1)' }} />
                  )}
                  {(values.length > 1) && (
                    <DeleteOutlined className="icon-item icon-pointer" onClick={() => this.deleteItem(index)} />
                  )}
                  {(index === values.length - 1) && (
                    <PlusOutlined className="icon-item icon-pointer" onClick={this.addItem} />
                  )}
                </Form.Item>
              </div>
            </Col>
          </Row>
        ))}
      </Form>
    );
  }
}
