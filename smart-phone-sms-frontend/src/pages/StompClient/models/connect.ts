import { ConnectModelType } from '@/models/connect';
import { Client } from 'stompjs';

interface State {
  client: Client | null
  connecting: boolean
}

const ConnectModel: ConnectModelType<State> = {
  namespace: 'Connect',
  state: {
    client: null,
    connecting: false,
  },
  effects: {
    * state({ payload }, { put }) {
      yield put({ type: 'setState', payload });
    },
  },
  reducers: {
    setState(state, { payload }) {
      return { ...state, ...payload } as State;
    },
  },
};
export default ConnectModel;
