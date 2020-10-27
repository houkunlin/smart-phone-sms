import { MenuDataItem, Settings as ProSettings } from '@ant-design/pro-layout';
import { Action, Effect, Loading, Reducer } from '@@/plugin-dva/connect';

export interface AnyAction extends Action {
  payload: any

  // Allows any extra properties to be defined in an action.
  [extraProps: string]: any
}

export interface ConnectState {
  loading: Loading;
  settings: ProSettings;

  // 其他未定义的数据
  [key: string]: any;
}

export interface Route extends MenuDataItem {
  routes?: Route[];
}

export interface ConnectModelType<STATE = any> {
  namespace: string;
  state: STATE;
  effects: {
    [key: string]: Effect
  };
  reducers: {
    [key: string]: Reducer<STATE, AnyAction>;
  };
}
