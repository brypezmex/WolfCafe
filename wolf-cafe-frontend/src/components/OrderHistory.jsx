import { useState, Fragment, useEffect } from 'react'
import '../css/OrderHistory.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import axios from 'axios'
import { getToken, getLoggedInUser } from '../services/AuthService'
import { apiUrl } from '../services/api'

function OrderHistory() {
  const [expandedOrders, setExpandedOrders] = useState({})
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')

  const sidebarData = {
    Order: {
      Menu: '/menu'
    },
    History: {
      'Current Orders': '/current',
      'Order History': '/history'
    }
  }

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await axios.get(apiUrl('/orders/my'), {
          headers: { Authorization: `Bearer ${getToken()}` }
        })

        const filtered = response.data.filter(
          order => order.status === 'PICKED_UP' || order.status === 'CANCELLED'
        )

        setOrders(filtered)
        setError('')
      } catch (err) {
        console.error('Failed to fetch orders:', err)
        setError('Failed to load order history.')
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

  const formatDate = (placedAt) => {
    const date = new Date(placedAt)
    return date.toLocaleDateString([], {
      month: '2-digit',
      day: '2-digit',
      year: 'numeric'
    })
  }

  const formatCurrency = (amount) => `$${Number(amount).toFixed(2)}`

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header name={getLoggedInUser()} />
        <div id='main-container'>
          

          {error && <p style={{ color: '#c0392b' }}>{error}</p>}

          <div className='table-wrap'>
            <table>
              <thead>
                <tr>
                  <th>Order Number</th>
                  <th>Order Date</th>
                  <th>Total</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>

              <tbody>
                {orders.map((order) => (
                  <Fragment key={order.id}>
                    <tr className={expandedOrders[order.id] ? 'row-expanded' : ''}>
                      <td>{String(order.id).padStart(7, '0')}</td>
                      <td>{formatDate(order.placedAt)}</td>
                      <td>{formatCurrency(order.total)}</td>
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
                        <td colSpan='4'>
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
                                <span>Tax ({(Number(order.taxRate) * 100).toFixed(2)}%)</span>
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
                    <td colSpan='4'>No past orders.</td>
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

export default OrderHistory