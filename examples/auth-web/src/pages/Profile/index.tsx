import React, { useState, useEffect } from 'react';
import { RouteComponentProps } from 'react-router';
import UserStore from '../../stores/user/UserStore';
import AuthStore from '../../stores/auth/AuthStore';
import { STORES } from '../../constants';
import { inject, observer } from 'mobx-react';
import './index.scss';

/* eslint-disable jsx-a11y/accessible-emoji */

interface InjectedProps {
  [STORES.USER_STORE]: UserStore;
  [STORES.AUTH_STORE]: AuthStore;
}

function Profile(props:InjectedProps & RouteComponentProps){

  const {authStore, userStore,history } = props;
  console.log(20, userStore);

  const handleLogOut = async(e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      authStore.signOut();
      history.push('/');
    } catch (err) {
      alert(err);
      console.log(err);
    }
  }

  useEffect(() => {
    const getUserData = async() => {
     await userStore.getUserProfile();
    }
    getUserData();
  })

  const gender = userStore.gender === 'Female' ? 'female' : 'male'
  const url = `/assets/photos/profile_${gender}.jpg`;

  return (

    <div className="profile-container">
      <div className="profile-block">
        <div className="profile-box">
          <img className="profile-photo" alt="프로필" src={url}/>
        </div>
        <div className="profile-info">
          <div className="profile-name">{userStore.name}</div>
          <div className="profile-email">📭<b>이메일: </b>{userStore.email}</div>
          <div className="profile-country">🌍<b>국적: </b>{userStore.country}</div>
          <div className="profile-job">💼<b>직업: </b>{userStore.job}</div>
        </div>
      </div>
      <div className="profile-buttons">
        <button onClick={handleLogOut} className="profile-logout-btn">
          로그아웃
        </button>
      </div>
    </div>
  )
}


export default inject(STORES.USER_STORE, STORES.AUTH_STORE)(observer(Profile));
