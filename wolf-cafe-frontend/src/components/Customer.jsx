import { useState, useEffect } from 'react'
import '../css/Staff.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import UserModal from './UserModal.jsx'
import axios from 'axios'
import { getToken } from '../services/AuthService'
import { apiUrl } from '../services/api'

const BASE_URL = apiUrl('/users')

function Customer() {
  const sidebarData = {
    Users: {
      Staff: '/staff',
      Customers: '/customer'
    },
    Config: {
      'Tax Rate': '/tax'
    }
  }

  const [showModal, setShowModal] = useState(false)
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [customers, setCustomers] = useState({})
  const [error, setError] = useState('')

  useEffect(() => {
    fetchCustomers()
  }, [])

  const fetchCustomers = async () => {
    try {
      const response = await axios.get(BASE_URL, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      const customerMap = {}

      response.data
        .filter(user => user.role === 'ROLE_CUSTOMER')
        .forEach(user => {
          customerMap[user.username] = {
            id: user.id,
            name: user.name,
            email: user.email
          }
        })

      setCustomers(customerMap)
      setError('')
    } catch (err) {
      console.error('Failed to fetch customers:', err)
      setError('Failed to load customers.')
    }
  }

  const handleEditClick = (username) => {
    setSelectedCustomer({ ...customers[username], username })
    setShowModal(true)
    setError('')
  }

  const handleSaveCustomer = async (userData) => {
    if (!selectedCustomer?.id) {
      setError('Cannot edit customer because the customer id is missing.')
      return
    }

    try {
      const response = await axios.put(
        `${BASE_URL}/${selectedCustomer.id}`,
        {
          name: userData.name,
          email: userData.email,
          username: userData.username
        },
        {
          headers: { Authorization: `Bearer ${getToken()}` }
        }
      )

      setCustomers(prev => {
        const updated = { ...prev }

        if (selectedCustomer.username !== userData.username) {
          delete updated[selectedCustomer.username]
        }

        updated[response.data.username ?? userData.username] = {
          id: response.data.id ?? selectedCustomer.id,
          name: response.data.name ?? userData.name,
          email: response.data.email ?? userData.email
        }

        return updated
      })

      setShowModal(false)
      setSelectedCustomer(null)
      setError('')
    } catch (err) {
      console.error('Failed to update customer:', err)
      setError(err.response?.data?.message || 'Failed to update customer.')
    }
  }

  const handleDeleteCustomer = async (username) => {
    const customer = customers[username]

    if (!customer?.id) {
      setError('Cannot delete customer because the customer id is missing.')
      return
    }

    try {
      await axios.delete(`${BASE_URL}/${customer.id}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      setCustomers(prev => {
        const updated = { ...prev }
        delete updated[username]
        return updated
      })

      setError('')
    } catch (err) {
      console.error('Failed to delete customer:', err)
      setError(err.response?.data?.message || 'Failed to delete customer.')
    }
  }

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header />
        <div id='main-container'>
          

          {error && <p style={{ color: '#c0392b' }}>{error}</p>}

          <div className='table-wrap'>
            <table>
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Edit</th>
                  <th>Delete</th>
                </tr>
              </thead>

              <tbody>
                {Object.entries(customers).map(([username, data]) => (
                  <tr key={username}>
                    <td>{username}</td>
                    <td>{data.name}</td>
                    <td>{data.email}</td>
                    <td>
                      <button id='edit-button' onClick={() => handleEditClick(username)}>
                        Edit
                      </button>
                    </td>
                    <td>
                      <button className='delete' onClick={() => handleDeleteCustomer(username)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}

                {Object.keys(customers).length === 0 && (
                  <tr>
                    <td colSpan='5'>No customers found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* No add button here because customers self-register. */}
        </div>
      </div>

      {showModal && (
        <UserModal
          initialData={selectedCustomer}
          onClose={() => setShowModal(false)}
          onSave={handleSaveCustomer}
        />
      )}
    </div>
  )
}

export default Customer