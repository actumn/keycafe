import AuthStore from './auth/AuthStore'
import UserStore from './user/UserStore'

export default class RootStore {
  static instance: RootStore;

  authStore = new AuthStore();
  userStore = new UserStore();
}
