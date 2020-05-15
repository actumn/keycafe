/* eslint-disable react/button-has-type */
import React, { ChangeEvent, useState } from 'react';

import './index.scss';

function SignIn() {
  const [, setEmail] = useState('');
  const [, setPassword] = useState('');

  const changeEmail = (e: ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
    // authStore.setEmail(e.target.value);
  };
  const changePassword = (e: ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
    // authStore.setPassword(e.target.value);
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
          <button className="btn btn-block btn-primary">
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

export default SignIn;
