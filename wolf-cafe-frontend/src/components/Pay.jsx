import { useState, useEffect } from 'react'
import '../css/Common.css'
import '../css/Pay.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import { useNavigate, useLocation } from 'react-router-dom'
import axios from 'axios'
import { getToken, getLoggedInUser } from '../services/AuthService'
import { API_BASE_URL } from '../services/api'

const BASE_URL = API_BASE_URL

function Pay() {
  const navigate = useNavigate()
  const { state } = useLocation()
  const { orderItems, total, cart } = state || { orderItems: [], total: 0, cart: {} }

  const sidebarData = {
    Order: { Menu: '/menu' },
    History: {
      'Current Orders': '/current',
      'Order History': '/history'
    }
  }

  const [tip, setTip] = useState(15)
  const [taxRate, setTaxRate] = useState(0)
  const [customTip, setCustomTip] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchTaxRate = async () => {
      try {
        const response = await axios.get(`${BASE_URL}/tax`, {
          headers: { Authorization: `Bearer ${getToken()}` }
        })
        setTaxRate(Number(response.data.rate) || 0)
      } catch (err) {
        console.error('Failed to fetch tax rate:', err)
      }
    }
    fetchTaxRate()
  }, [])

  const tipPercent = customTip !== '' ? Number(customTip) : tip
  const safeTipPercent = Number.isFinite(tipPercent) && tipPercent >= 0 ? tipPercent : 0

  const taxAmount = total * taxRate
  const tipAmount = total * (safeTipPercent / 100)
  const grandTotal = total + taxAmount + tipAmount

  const handlePay = async () => {
    setError('')

    if (!orderItems || orderItems.length === 0) {
      setError('Your order is empty.')
      return
    }

    try {
      await axios.post(
        `${BASE_URL}/orders`,
        {
          items: orderItems.map(item => ({
            recipeId: item.recipeId,
            quantity: item.quantity
          })),
          tipPercent: safeTipPercent
        },
        { headers: { Authorization: `Bearer ${getToken()}` } }
      )
      navigate('/current')
    } catch (err) {
      console.error('Failed to place order:', err)
      setError(err.response?.data?.message || 'Failed to place order.')
    }
  }

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header name={getLoggedInUser()} />
        <div id='main-container'>

          <div id='pay-container'>
            <button
              id='pay-back-button'
              onClick={() => navigate('/menu', { state: { cart } })}
            >
              ←
            </button>

            {/* Order Summary */}
            <div id='order-summary-container'>
              <div id='summary'>
                {orderItems.map((item) => (
                  <div key={item.recipeId} className='summary-row'>
                    <span className='summary-name'>{item.recipeName}</span>
                    <span className='summary-qty'>x{item.quantity}</span>
                    <span className='summary-price'>${Number(item.priceEach).toFixed(2)} each</span>
                  </div>
                ))}
              </div>

              <div id='tax-summary'>
                <p id='pretax-total'>Subtotal: ${total.toFixed(2)}</p>
                <p>Sales Tax ({(taxRate * 100).toFixed(2)}%): ${taxAmount.toFixed(2)}</p>
                <p>Tip ({safeTipPercent}%): ${tipAmount.toFixed(2)}</p>
              </div>

              <h2><strong>Total: ${grandTotal.toFixed(2)}</strong></h2>

              {error && <p className='pay-error'>{error}</p>}
            </div>

            {/* Checkout */}
            <div id='pay-checkout'>
              <p id='pay-tip'>Tip</p>

              <div id='tip-buttons'>
                {[15, 20, 25].map(t => (
                  <button
                    key={t}
                    className={tip === t && customTip === '' ? 'active' : ''}
                    onClick={() => { setTip(t); setCustomTip('') }}
                  >
                    {t}%
                  </button>
                ))}
              </div>

              <div id='custom-tip-wrapper'>
                <label htmlFor='custom-tip'>Custom %</label>
                <input
                  id='custom-tip'
                  type='number'
                  min='0'
                  step='0.1'
                  value={customTip}
                  placeholder='0'
                  onChange={e => setCustomTip(e.target.value)}
                />
              </div>

              <button id='pay-button' onClick={handlePay}>Pay ${grandTotal.toFixed(2)}</button>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}

export default Pay