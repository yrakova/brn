import Service from '@ember/service';
import fetch from 'fetch';
import { inject as service } from '@ember/service';
import Session from 'ember-simple-auth/services/session';
import Store from '@ember-data/store';
import { DateTime } from 'luxon';

interface UserDTO {
  firstName: string;
  lastName: string;
  email: string;
  avatar: string;
  birthday: string;
  password?: string;
  id?: string;
}

export interface LatestUserDTO {
  name: string;
  email: string;
  password: string;
  gender: 'MALE' | 'FEMALE';
  bornYear: number;
  avatar: string;
  id?: string;
}

function fromLatestUserDto(user: LatestUserDTO): UserDTO {
  const [firstName = '', lastName = ''] = (user.name || '').split(' ');
  return {
    firstName: firstName || '',
    lastName: lastName || '',
    avatar: user.avatar,
    email: user.email,
    birthday: new Date().setFullYear(user.bornYear).toString(),
    id: user.id as string,
  };
}

function formatDateForRequest(date: Date): string {
  return DateTime.fromJSDate(date).toUTC().toFormat('yyyy-MM-dd');
}

export default class NetworkService extends Service {
  @service('session') session!: Session;
  @service('store') store!: Store;
  prefix = '/api';
  get _headers() {
    return Object.assign(
      {
        'Content-Type': 'application/json',
      },
      this.store.adapterFor('application').headers,
    );
  }
  postRequest(entry: string, data: unknown) {
    return fetch(`${this.prefix}/${entry}`, {
      body: JSON.stringify(data),
      headers: this._headers,
      method: 'POST',
    });
  }
  request(entry: string) {
    return fetch(`${this.prefix}/${entry}`, {
      headers: this._headers,
      method: 'GET',
    });
  }
  patch(entry: string, data: unknown) {
    return fetch(`${this.prefix}/${entry}`, {
      headers: this._headers,
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }
  async cloudUrl() {
    let result = await this.request('cloud/baseFileUrl');
    let { data } = await result.json();
    return data;
  }
  async getCurrentUser() {
    try {
      let result = await this.request('users/current');
      let { data } = await result.json();
      return fromLatestUserDto(Array.isArray(data) ? data[0] : data);
    } catch (e) {
      await this.session.invalidate();
      throw e;
    }
  }
  async patchUserInfo(userInfo: LatestUserDTO): Promise<LatestUserDTO> {
    let result = await this.patch('users/current', userInfo);
    let { data } = await result.json();
    return data;
  }
  async loadCurrentUser() {
    const user: any = await this.getCurrentUser();
    user.initials = `${user.firstName.charAt(0)}${user.lastName.charAt(
      0,
    )}`.toUpperCase();
    this.session.set('data.user', user);
  }
  createUser(user: LatestUserDTO) {
    return this.postRequest('registration', user);
  }
  async subgroupStats(id: string) {
    let result = await this.request(`statistics/subgroups?ids=${id}`);
    let { data } = await result.json();
    return data[0];
  }
  async availableExercises(ids: string[]) {
    const result = await this.postRequest(`exercises/byIds`, {
      ids: ids.map((el) => parseInt(el, 10)),
    });
    const json = await result.json();
    const { data } = json;
    return data.map((el: number) => String(el));
  }
  async getUserStatisticsByWeek(from: Date, to: Date) {
    const fromFormatted: string = formatDateForRequest(from);
    const toFormatted: string = formatDateForRequest(to);
    const result = await this.request(
      `statistics/study/week?from=${fromFormatted}&to=${toFormatted}`,
    );
    let { data } = await result.json();
    return data;
  }
  async getUserStatisticsByYear(from: Date, to: Date) {
    const fromFormatted: string = formatDateForRequest(from);
    const toFormatted: string = formatDateForRequest(to);
    const result = await this.request(
      `statistics/study/year?from=${fromFormatted}&to=${toFormatted}`,
    );
    let { data } = await result.json();
    return data;
  }
}

// DO NOT DELETE: this is how TypeScript knows how to look up your services.
declare module '@ember/service' {
  interface Registry {
    network: NetworkService;
  }
}
