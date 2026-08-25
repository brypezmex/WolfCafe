import axios from 'axios'
import { getToken } from './AuthService'
import { apiUrl } from './api'

const BASE_REST_API_URL = apiUrl('/recipes')

const authHeader = () => ({ Authorization: `Bearer ${getToken()}` })

export const saveRecipe = (recipe) =>
  axios.post(BASE_REST_API_URL, recipe, { headers: authHeader() })

export const getAllRecipes = () =>
  axios.get(BASE_REST_API_URL, { headers: authHeader() })

export const getRecipeByName = (name) =>
  axios.get(`${BASE_REST_API_URL}/${name}`, { headers: authHeader() })

export const updateRecipe = (id, recipe) =>
  axios.put(`${BASE_REST_API_URL}/${id}`, recipe, { headers: authHeader() })

export const deleteRecipeById = (id) =>
  axios.delete(`${BASE_REST_API_URL}/${id}`, { headers: authHeader() })