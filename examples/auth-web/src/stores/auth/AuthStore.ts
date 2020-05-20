import { action, observable, reaction } from 'mobx';
//import LoginService, { LoginRequestDto } from '../../services/LoginService'
import AuthService, { LoginRequestDto } from '../../services/AuthService';
import autobind from 'autobind-decorator';


@autobind
class AuthStore {
  @observable token: string | null = window.sessionStorage.getItem('keycafe-token');
  @observable email = '';
  @observable password = '';

  private loginService = new AuthService();

  constructor() {
    reaction(
      () => this.token,
      token => {
        if (token != null) window.sessionStorage.setItem('keycafe-token', token);
      }
    );
  }

  isLoggedIn() {
    return this.token != null;
  }

  @action
  async login() {
    const body: LoginRequestDto = {
      email: this.email,
      password: this.password
    };
    console.log(46, body);
    const response = await this.loginService.login(body);
    console.log(47, response);
    this.setToken(response.data['token']);
  }


  @action
  resetPasswordAndEmail() {
    this.password = '';
    this.email = '';
  }

  @action
  setPassword(pw: string) {
    this.password = pw;
  }

  @action
  setEmail(email: string) {
    this.email = email;
  }

  @action
  setToken(token: string) {
    this.token = token;
  }

  @action
  signOut() {
    window.sessionStorage.removeItem('keycafe-token');
    this.token = null;
  }

}

export default AuthStore;
