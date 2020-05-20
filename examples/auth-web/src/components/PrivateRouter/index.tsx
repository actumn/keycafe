import React from 'react';
import { Redirect, Route} from 'react-router-dom';
import { inject, observer } from 'mobx-react';
import AuthStore from '../../stores/auth/AuthStore';

interface PrivateRouterProps {
  authStore?: AuthStore;
  component: React.ComponentType<any>;
  redirectTo: string;
  path: string;
  exact?: boolean;
}

export default inject('authStore')(
  observer(
    ({ component: Component, redirectTo, authStore, path, exact }: PrivateRouterProps) => {
      return (
        <Route
          path={path}
          exact={exact}
          render={(props: any) =>
            authStore!.isLoggedIn() ? (
              <Component {...props} />
            ) : (
              <Redirect
                to={redirectTo}
              />
            )
          }
        />
      );
    }
  )
);
