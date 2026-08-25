import SidebarNode from "./SidebarNode"
import '../css/Sidebar.css'
import { useNavigate, useLocation } from 'react-router-dom'
import logo from '../assets/wolf-head.png'

function Sidebar({data}) {
  const navigate = useNavigate()
  const { pathname } = useLocation()

  const handleLogout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }

  return (
    <div id='sidebar-container'>
      <div id='sidebar-top'>
        <div id='sidebar-logo'>
          <h1>WolfCafe</h1>
          <img src={logo}/>

        </div>
        
        {Object.entries(data).map(([category, items]) => (
          <SidebarNode key={category} category={category} items={items} currentPath={pathname} />
        ))}
      </div>
      <p onClick={handleLogout}>Log Out</p>
    </div>
  )
}

export default Sidebar