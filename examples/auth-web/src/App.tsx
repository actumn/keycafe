import React, { Component } from 'react';
import { BrowserRouter, Switch, Route, Redirect } from 'react-router-dom';
import autobind from 'autobind-decorator';
import { inject, observer } from 'mobx-react';
import { PAGE_PATHS, STORES } from './constants';
import PrivateRoute from './components/PrivateRouter';
import { SignIn, Profile } from './pages';

@inject(STORES.AUTH_STORE)
@observer
@autobind
export default class App extends Component {
  render() {
    return (
      <BrowserRouter>
        <Switch>
          <Route path={PAGE_PATHS.SIGNIN} component={SignIn} />
          <PrivateRoute
              path={PAGE_PATHS.PROFILE}
              redirectTo={PAGE_PATHS.SIGNIN}
              component={Profile}
          />
          <Redirect from="/" to={PAGE_PATHS.PROFILE} />
        </Switch>
    </BrowserRouter>
    );
  }
}
