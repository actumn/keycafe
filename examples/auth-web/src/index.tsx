import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import StoreProvider from './providers/StoreProvider';
import './styles/index.scss';

ReactDOM.render(
  <StoreProvider>
    <App />
  </StoreProvider>,
  document.getElementById('root')
);
