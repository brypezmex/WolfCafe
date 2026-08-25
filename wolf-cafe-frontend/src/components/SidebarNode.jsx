import { useNavigate, useLocation } from 'react-router-dom'

function SidebarNode({ category, items }) {
  const navigate = useNavigate()
  const { pathname } = useLocation()

  return (
    <div id='sidebarNode-container'>
      <h2 className='node-category'>{category}</h2>
      <ul className='node-item-list'>
        {Object.entries(items).map(([label, path]) => (
          <li
            key={label}
            className={`node-item-li${pathname === path ? ' active' : ''}`}
            onClick={() => navigate(path)}
          >
            {label}
          </li>
        ))}
      </ul>
    </div>
  )
}

export default SidebarNode