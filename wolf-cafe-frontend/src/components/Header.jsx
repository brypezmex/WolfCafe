import '../css/Header.css'
import { getLoggedInUser } from '../services/AuthService'

function Header({ name }) {
  const user = getLoggedInUser() || ''

  return (
    <header>
          <h1 id='user-icon'>{user.charAt(0).toUpperCase()}</h1>
          <p id='username'>{user}</p>
    </header>
  )
}

export default Header