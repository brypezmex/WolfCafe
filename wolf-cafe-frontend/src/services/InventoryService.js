import axios from 'axios'
import { getToken } from './AuthService'
import { apiUrl } from './api'

const BASE_REST_API_URL = apiUrl('/inventory')

axios.interceptors.request.use(function (config) {
  config.headers['Authorization'] = 'Bearer ' + getToken()
  return config;
}, function ( err ) {
  return Promise.reject( err );
});

export const getInventory = () => axios.get(BASE_REST_API_URL, {
  headers: { Authorization: `Bearer ${getToken()}` }
})

export const updateInventory = (ingredients) => 
  axios.put(BASE_REST_API_URL, { ingredients }, {
    headers: { Authorization: `Bearer ${getToken()}` }
  })
