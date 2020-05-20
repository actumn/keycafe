import * as React from 'react';
import { Provider } from 'mobx-react';
import RootStore from '../stores/RootStore';

interface IStoreProviderProps {}

const rootStore = new RootStore();



const StoreProvider: React.FunctionComponent<IStoreProviderProps> = ({
  children
}) => {
  return (<Provider {...rootStore}>{children}</Provider>);
} 


export default StoreProvider;
