import '../css/Register.css'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerAPICall } from '../services/AuthService'
import logo from '../assets/wolf-head.png'

function Register() {
  const navigate = useNavigate()

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: ''
  })

  const [error, setError] = useState('')

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleRegister = async () => {
    setError('')

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    const registerObj = {
      name: formData.name,
      email: formData.email,
      username: formData.username,
      password: formData.password
    }

    try {
      await registerAPICall(registerObj)
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.')
    }
  }

  return (
    <div id='register-background'>
      <div id="register-container">
        <div id="logo-container">
          <h1 id='register-logo'>WolfCafe</h1>
          <img src={logo}/>
        </div>
        <div id='register-form'>
          <input
            id='register-name'
            type="text"
            placeholder="Name"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
          />
          <input
            id='register-email'
            type="text"
            placeholder="Email"
            value={formData.email}
            onChange={(e) => handleChange('email', e.target.value)}
          />
          <input
            id='register-username'
            type="text"
            placeholder="Username"
            value={formData.username}
            onChange={(e) => handleChange('username', e.target.value)}
          />
          <input
            id='register-password'
            type="password"
            placeholder="Password"
            value={formData.password}
            onChange={(e) => handleChange('password', e.target.value)}
          />
          <input
            id='register-confirm'
            type="password"
            placeholder="Confirm Password"
            value={formData.confirmPassword}
            onChange={(e) => handleChange('confirmPassword', e.target.value)}
          />
          {error && <p id='register-error'>{error}</p>}
          <button id='register-button' onClick={handleRegister}>Register</button>
          <p id='register-login-button' onClick={() => navigate('/login')}>Already Have an Account? Login</p>
        </div>
      </div>
    </div>
  )
}

export default Register