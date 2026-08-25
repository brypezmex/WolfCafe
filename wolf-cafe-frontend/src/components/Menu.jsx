import { useState, useEffect } from 'react'
import '../css/Common.css'
import '../css/Menu.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import MenuNode from './MenuNode.jsx'
import { useNavigate, useLocation } from 'react-router-dom'
import axios from 'axios'
import { getToken, getLoggedInUser } from '../services/AuthService'
import { apiUrl } from '../services/api'

export function getRandomCoffeePic() {
  const num = Math.floor(Math.random() * 29) + 1
  return new URL(`../assets/coffee-pics-optimized/coffee-${num}.webp`, import.meta.url).href
}

function Menu() {
  const navigate = useNavigate()
  const location = useLocation()
  const [items, setItems] = useState([])
  const [pics, setPics] = useState({})
  const [cart, setCart] = useState({})

  const sidebarData = {
    'Order': { 'Menu': '/menu' },
    'History': {
      'Current Orders': '/current',
      'Order History': '/history'
    }
  }

  useEffect(() => {
    const fetchRecipes = async () => {
      try {
        const response = await axios.get(apiUrl('/recipes'), {
          headers: { Authorization: `Bearer ${getToken()}` }
        })
        const recipes = response.data
        setItems(recipes)
        setPics(recipes.reduce((acc, recipe) => ({ ...acc, [recipe.id]: getRandomCoffeePic() }), {}))

        const savedCart = location.state?.cart
        setCart(savedCart || recipes.reduce((acc, recipe) => ({ ...acc, [recipe.id]: 0 }), {}))
      } catch (err) {
        console.error('Failed to fetch recipes:', err)
      }
    }
    fetchRecipes()
  }, [])

  const updateCart = (id, delta) => {
    setCart(prev => ({
      ...prev,
      [id]: Math.max(0, (prev[id] || 0) + delta)
    }))
  }

  const total = items.reduce((sum, item) => sum + (cart[item.id] || 0) * item.price, 0)

  const handleCheckout = () => {
    const orderItems = items
      .filter(item => cart[item.id] > 0)
      .map(item => ({
        recipeId: item.id,
        recipeName: item.name,
        priceEach: item.price,
        quantity: cart[item.id]
      }))
    navigate('/pay', { state: { orderItems, total, cart } })
  }

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header name={getLoggedInUser()} />
        <div id="main-container">
          <div id='menu-container'>
            {items.map((item) => (
              <MenuNode
                key={item.id}
                name={item.name}
                price={item.price}
                ingredients="none"
                pic={pics[item.id]}
                count={cart[item.id] || 0}
                onAdd={() => updateCart(item.id, 1)}
                onRemove={() => updateCart(item.id, -1)}
              />
            ))}
          </div>
          <div id='menu-checkout'>
            <p id='menu-total'>Current Total: ${total.toFixed(2)}</p>
            <button id='menu-pay-button' onClick={handleCheckout}>Review and Pay</button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Menu