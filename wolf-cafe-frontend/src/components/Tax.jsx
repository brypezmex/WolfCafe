import { useState, useEffect } from 'react'
import '../css/Tax.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import axios from 'axios'
import { getToken } from '../services/AuthService'
import { API_BASE_URL } from '../services/api'

const BASE_URL = API_BASE_URL

function Tax() {
  const sidebarData = {
    Users: {
      Staff: '/staff',
      Customers: '/customer'
    },
    Config: {
      'Tax Rate': '/tax'
    }
  }

  const [taxRate, setTaxRate] = useState(2.0)
  const [saveStatus, setSaveStatus] = useState('')

  useEffect(() => {
    const fetchTaxRate = async () => {
      try {
        const response = await axios.get(`${BASE_URL}/tax`, {
          headers: { Authorization: `Bearer ${getToken()}` }
        })

        // Backend stores tax as decimal, for example 0.02.
        // Frontend displays it as percent, for example 2.0.
        setTaxRate((parseFloat(response.data.rate) || 0) * 100)
      } catch (err) {
        console.error('Error fetching tax rate:', err)
      }
    }

    fetchTaxRate()
  }, [])

  const handleSlider = (e) => {
    setTaxRate(parseFloat(e.target.value))
  }

  const handleInput = (e) => {
    const val = Math.min(25, Math.max(0, parseFloat(e.target.value) || 0))
    setTaxRate(val)
  }

  const handleSave = async () => {
    setSaveStatus('saving')

    try {
      // Backend expects decimal, for example 0.02.
      await axios.put(
        `${BASE_URL}/tax`,
        { rate: taxRate / 100 },
        {
          headers: { Authorization: `Bearer ${getToken()}` }
        }
      )

      setSaveStatus('saved')
      setTimeout(() => setSaveStatus(''), 2000)
    } catch (err) {
      console.error('Failed to save tax rate:', err)
      setSaveStatus('error')
      setTimeout(() => setSaveStatus(''), 2000)
    }
  }

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header />
        <div id='main-container'>
          <div className='tax-rate-container'>
            <h2>Current Tax Rate</h2>

            <div className='tax-rate-number-row'>
              <input
                type='number'
                min='0'
                max='25'
                step='0.1'
                value={taxRate}
                onChange={handleInput}
              />
              <span>%</span>
            </div>

            <div className='tax-rate-slider-wrapper'>
              <input
                type='range'
                min='0'
                max='25'
                step='0.1'
                value={taxRate}
                onChange={handleSlider}
                style={{ '--val': (taxRate / 25) * 100 }}
              />
              <div className='tax-rate-labels'>
                <span>0%</span>
                <span>25%</span>
              </div>
            </div>

            <button
              id='save-tax-button'
              onClick={handleSave}
              disabled={saveStatus === 'saving'}
            >
              {saveStatus === 'saving'
                ? 'Saving...'
                : saveStatus === 'saved'
                  ? 'Saved'
                  : saveStatus === 'error'
                    ? 'Error'
                    : 'Save'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Tax