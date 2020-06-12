import { action, observable } from 'mobx';
import UserService from '../../services/UserService';
import autobind from 'autobind-decorator';

@autobind
class UserStore {
  @observable token: string | null = window.sessionStorage.getItem('keycafe-token');
 
  @observable name = '';
  @observable gender = '';
  @observable email = '';
  @observable country = '';
  @observable job = '';

  private userService = new UserService();

 @action
 setEmail(email: string) {
   this.email = email;
 }

 @action
 setName(name: string) {
   this.name = name;
 }

 @action
 setGender(gender: string) {
   this.gender = gender;
 }

 @action
 setJob(job: string) {
   this.job = job;
 }

 @action
 setCountry(country: string) {
   this.country = country;
 }


  @action
  async getUserProfile() {
    const keycafeToken = window.sessionStorage.getItem('keycafe-token') || '';
    console.log(78, keycafeToken);
    const response = await this.userService.getUserProfile(keycafeToken);
    console.log(47, response);
    this.setEmail(response.data['email']);
    this.setName(response.data['name']);
    this.setGender(response.data['gender']);
    this.setCountry(response.data['country']);
    this.setJob(response.data['job']);
  }
}

export default UserStore;