import { useState, Fragment, useEffect } from 'react'
import '../css/CurrentOrders.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import axios from 'axios'
import { getToken, getLoggedInUser } from '../services/AuthService'
import { apiUrl } from '../services/api'

function CurrentOrders() {
  const [expandedOrders, setExpandedOrders] = useState({})
  const [orders, setOrders] = useState([])

  const sidebarData = {
    Order: {
      Menu: '/menu'
    },
    History: {
      'Current Orders': '/current',
      'Order History': '/history'
    }
  }

  const confirmOrder = async (orderId) => {
    try {
      await axios.put(apiUrl(`/orders/${orderId}/pickup`), {}, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      setOrders(prev => prev.filter(o => o.id !== orderId))
    } catch (err) {
      console.error('Failed to confirm pickup:', err)
    }
  }

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await axios.get(apiUrl('/orders/my'), {
          headers: { Authorization: `Bearer ${getToken()}` }
        })

        const filtered = response.data.filter(
          o => o.status === 'PENDING' || o.status === 'FULFILLED'
        )

        setOrders(filtered)
      } catch (err) {
        console.error('Failed to fetch orders:', err)
      }
    }

    fetchOrders()
  }, [])

  const toggleOrder = (orderId) => {
    setExpandedOrders(prev => ({
      ...prev,
      [orderId]: !prev[orderId]
    }))
  }

  const formatTime = (placedAt) => {
    const date = new Date(placedAt)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatCurrency = (amount) => `$${Number(amount).toFixed(2)}`

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header name={getLoggedInUser()} />
        <div id='main-container'>
          <div className='table-wrap'>
            <table>
              <thead>
                <tr>
                  <th>Order Number</th>
                  <th>Order Time</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>&nbsp;</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>

              <tbody>
                {orders.map((order) => (
                  <Fragment key={order.id}>
                    <tr className={expandedOrders[order.id] ? 'row-expanded' : ''}>
                      <td>{String(order.id).padStart(7, '0')}</td>
                      <td>{formatTime(order.placedAt)}</td>
                      <td>{formatCurrency(order.total)}</td>
                      <td>{order.status}</td>
                      <td>
                        {order.status === 'FULFILLED' && (
                          <button
                            className='confirm-button'
                            onClick={() => confirmOrder(order.id)}
                          >
                            Confirm Pickup
                          </button>
                        )}
                      </td>
                      <td
                        className='history-view-button'
                        onClick={() => toggleOrder(order.id)}
                        style={{ cursor: 'pointer' }}
                      >
                        {expandedOrders[order.id] ? 'Hide Order' : 'View Order'}
                      </td>
                    </tr>

                    {expandedOrders[order.id] && (
                      <tr className='details-row'>
                        <td colSpan='6'>
                          <div className='order-details-rectangle'>
                            <h4 className='details-title'>Order Summary</h4>

                            <ul className='items-list'>
                              {order.items.map((item) => (
                                <li key={item.recipeId} className='item-line'>
                                  <span className='item-qty'>{item.quantity}x</span>{' '}
                                  {item.recipeName} — {formatCurrency(item.priceEach)} each
                                </li>
                              ))}
                            </ul>

                            <div className='order-financials'>
                              <div className='financial-line'>
                                <span>Subtotal</span>
                                <span>{formatCurrency(order.subtotal)}</span>
                              </div>

                              <div className='financial-line'>
                                <span>Tax ({(order.taxRate * 100).toFixed(2)}%)</span>
                                <span>{formatCurrency(order.taxAmount)}</span>
                              </div>

                              <div className='financial-line'>
                                <span>Tip ({Number(order.tipPercent).toFixed(0)}%)</span>
                                <span>{formatCurrency(order.tipAmount)}</span>
                              </div>

                              <div className='financial-line total-line'>
                                <span>Total</span>
                                <span>{formatCurrency(order.total)}</span>
                              </div>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                ))}

                {orders.length === 0 && (
                  <tr>
                    <td colSpan='6'>No current orders.</td>
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

export default CurrentOrders