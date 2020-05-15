import * as React from 'react';
import { BrowserRouter, Switch, Route } from 'react-router-dom';
import { SignIn } from './pages';

const App: React.FC = () => (
  <BrowserRouter>
    <Switch>
      <Route path="/" component={SignIn} />
    </Switch>
  </BrowserRouter>
);

export default App;
