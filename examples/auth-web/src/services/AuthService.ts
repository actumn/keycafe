import axios from 'axios';

export type LoginRequestDto = {
  email: string;
  password: string;
};

export type LoginResponseDto = {
  data: {
    token: string;
  }
}


class AuthService {
  async login(body: LoginRequestDto): Promise<LoginResponseDto> {
    return axios.post('/api/v1/login', body);
  }
}

export default AuthService;