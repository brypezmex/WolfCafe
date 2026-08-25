import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import '../css/Login.css'
import { loginAPICall, storeToken, saveLoggedInUser } from '../services/AuthService'
import logo from '../assets/wolf-head.png'

function Login() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleLogin = async () => {
    setError('')
    try {
      const response = await loginAPICall(username, password)
      const token = response.data.accessToken

      storeToken(token)
      saveLoggedInUser(username, response.data.role)

      const role = response.data.role
      if (role === 'ROLE_ADMIN') navigate('/tax')
      else if (role === 'ROLE_STAFF') navigate('/inventory')
      else navigate('/menu')

    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password.')
    }
  }

  return (
    <div id='login-background'>
      <div id='login-container'>
        <div id='logo-container'>
          <h1 id='login-logo'>WolfCafe</h1>
          <img src={logo}/>
        </div>
        <div id='login-form'>
          <input
            id='login-username'
            type='text'
            placeholder='Username'
            value={username}
            onChange={e => setUsername(e.target.value)}
          />
          <input
            id='login-password'
            type='password'
            placeholder='Password'
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
          {error && <p style={{ color: '#c0392b', fontSize: '0.85rem' }}>{error}</p>}
          <button id='login-button' onClick={handleLogin}>Login</button>
          <p id='login-register-button' onClick={() => navigate('/register')}>Register</p>
        </div>
      </div>
    </div>
  )
}

export default Login