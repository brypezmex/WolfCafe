import { useState, useEffect, Fragment } from 'react'
import '../css/ActiveOrders.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import axios from 'axios'
import { getToken } from '../services/AuthService'
import { API_BASE_URL } from '../services/api'

const BASE_URL = API_BASE_URL

function ActiveOrders() {
  const [expandedOrders, setExpandedOrders] = useState({})
  const [orders, setOrders] = useState([{}])
  const [error, setError] = useState('')

  const sidebarData = {
    Catalog: {
      Inventory: '/inventory',
      Recipes: '/recipes'
    },
    Orders: {
      'Active Orders': '/active'
    }
  }

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await axios.get(`${BASE_URL}/orders`, {
          headers: { Authorization: `Bearer ${getToken()}` }
        })

        const pending = response.data.filter(o => o.status === 'PENDING')
        setOrders(pending)
        setError('')
      } catch (err) {
        console.error('Failed to fetch orders:', err)
        setError('Failed to load active orders.')
      }
    }

    fetchOrders()
  }, [])

  const toggleOrder = (orderId) => {
    setExpandedOrders(prev => ({ ...prev, [orderId]: !prev[orderId] }))
  }

  const handleComplete = async (orderId) => {
    try {
      await axios.put(`${BASE_URL}/orders/${orderId}/fulfill`, {}, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })
      setOrders(prev => prev.filter(o => o.id !== orderId))
      setError('')
    } catch (err) {
      console.error('Failed to complete order:', err)
      setError('Failed to complete order.')
    }
  }

  const formatTime = (placedAt) => {
    const date = new Date(placedAt)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatOrderNum = (id) => String(id).padStart(7, '0')

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header />
        <div id='main-container'>
          {error && <p className='pageError'>{error}</p>}

          <div className='table-wrap'>
            <table className='orders-table'>
              <thead>
                <tr>
                  <th>&nbsp;</th>
                  <th>Order Number</th>
                  <th>Customer</th>
                  <th>Order Time</th>
                  <th>Cost</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>

              <tbody>
                {orders.map(order => (
                  <Fragment key={order.id}>
                    <tr className={expandedOrders[order.id] ? 'row-expanded' : ''}>
                      <td>
                        <button
                          className='expand-button'
                          onClick={() => toggleOrder(order.id)}
                        >
                          {expandedOrders[order.id] ? '−' : '+'}
                        </button>
                      </td>

                      <td>{formatOrderNum(order.id)}</td>
                      <td>{order.customerUsername}</td>
                      <td>{formatTime(order.placedAt)}</td>
                      <td>${Number(order.total).toFixed(2)}</td>

                      <td>
                        <button
                          className='complete-button'
                          onClick={() => handleComplete(order.id)}
                        >
                          Complete
                        </button>
                      </td>
                    </tr>

                    {expandedOrders[order.id] && (
                      <tr className='details-row'>
                        <td colSpan='6'>
                          <div className='order-details-rectangle'>
                            <h4 className='details-title'>Order Breakdown</h4>

                            <ul className='items-list'>
                              {(order.items || []).map(item => (
                                <li key={item.recipeId} className='item-line'>
                                  <span className='item-qty'>{item.quantity}x</span> {item.recipeName}
                                </li>
                              ))}
                            </ul>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                ))}

                {orders.length === 0 && (
                  <tr>
                    <td colSpan='6'>No active orders.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ActiveOrders