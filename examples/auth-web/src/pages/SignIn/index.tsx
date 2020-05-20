import React, { ChangeEvent } from 'react';
import { RouteComponentProps } from 'react-router';
import { inject, observer } from 'mobx-react';
import { STORES } from '../../constants';
import AuthStore from '../../stores/auth/AuthStore';

import './index.scss';

interface InjectedProps {
  [STORES.AUTH_STORE]: AuthStore;
}

function SignIn(props:InjectedProps & RouteComponentProps) {

  const { authStore, history } = props;

  const handleLogin = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      await authStore.login();
      history.push('/profile');
    } catch (err) {
      alert(err);
      console.log(err);
    }
  };

  

  const changeEmail = (e: ChangeEvent<HTMLInputElement>) => {
    authStore.setEmail(e.target.value);
    console.log(e.target.value);
  };
  const changePassword = (e: ChangeEvent<HTMLInputElement>) => {
    authStore.setPassword(e.target.value);
    console.log(e.target.value);
  };

  return (
    <div className="container container-sm container-sign">
      <form className="form-sign">
        <span role="img" aria-label="sheep" className="form-headline">🌾 로그인 🐑</span>
        <div className="form-group">
          <input
            type="text"
            className="form-control"
            onChange={changeEmail}
            placeholder="이메일 입력"
          />
        </div>
        <div className="form-group">
          <input
            type="password"
            className="form-control"
            onChange={changePassword}
            placeholder="비밀번호 입력"
          />
        </div>
        <div className="main-page-buttons">
          <button onClick={handleLogin} className="btn btn-block btn-primary">
            로그인
          </button>
        </div>
        <h6 className="txt-terms">
          이용약관
          {' '}
          및
          개인정보
          {' '}
          취급방침
        </h6>
      </form>
    </div>
  );
}

export default inject(STORES.AUTH_STORE)(observer(SignIn));