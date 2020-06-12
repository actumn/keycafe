import axios from 'axios';

export type ProfileResponseDto = {
  data: {
    name: string,
    gender: string,
    email: string,
    country: string,
    job: string
  }
}


class UserService {
  async getUserProfile(token: string): Promise<ProfileResponseDto> {
    return axios.get('/api/v1/user', { headers : { 'Authorization' : token }});
  }
}

export default UserService;