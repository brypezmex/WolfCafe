import { useState, useEffect } from 'react'
import '../css/Staff.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import UserModal from './UserModal.jsx'
import axios from 'axios'
import { getToken } from '../services/AuthService'
import { API_BASE_URL } from '../services/api'

const BASE_URL = API_BASE_URL

function Staff() {
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
  const [selectedUser, setSelectedUser] = useState(null)
  const [staff, setStaff] = useState({})
  const [error, setError] = useState('')

  useEffect(() => {
    fetchStaff()
  }, [])

  const fetchStaff = async () => {
    try {
      const response = await axios.get(`${BASE_URL}/users`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      const staffMap = {}

      response.data
        .filter(u => u.role === 'ROLE_STAFF')
        .forEach(u => {
          staffMap[u.username] = {
            id: u.id,
            name: u.name,
            email: u.email
          }
        })

      setStaff(staffMap)
    } catch (err) {
      console.error('Failed to fetch staff:', err)
      setError('Failed to load staff users.')
    }
  }

  const handleAddClick = () => {
    setSelectedUser(null)
    setShowModal(true)
    setError('')
  }

  const handleEditClick = (username) => {
    setSelectedUser({ ...staff[username], username })
    setShowModal(true)
    setError('')
  }

  const handleSaveUser = async (userData) => {
    const isEditing = !!selectedUser

    try {
      if (isEditing && selectedUser.id) {
        const response = await axios.put(
          `${BASE_URL}/users/${selectedUser.id}`,
          {
            name: userData.name,
            email: userData.email,
            username: userData.username
          },
          {
            headers: { Authorization: `Bearer ${getToken()}` }
          }
        )

        setStaff(prev => {
          const updated = { ...prev }

          if (selectedUser.username !== userData.username) {
            delete updated[selectedUser.username]
          }

          updated[response.data.username ?? userData.username] = {
            id: response.data.id ?? selectedUser.id,
            name: response.data.name ?? userData.name,
            email: response.data.email ?? userData.email
          }

          return updated
        })
      } else {
        const response = await axios.post(
          `${BASE_URL}/users/staff`,
          {
            name: userData.name,
            email: userData.email,
            username: userData.username,
            password: userData.password
          },
          {
            headers: { Authorization: `Bearer ${getToken()}` }
          }
        )

        setStaff(prev => ({
          ...prev,
          [response.data.username ?? userData.username]: {
            id: response.data.id,
            name: response.data.name ?? userData.name,
            email: response.data.email ?? userData.email
          }
        }))
      }

      setShowModal(false)
      setSelectedUser(null)
      setError('')
    } catch (err) {
      console.error('Failed to save staff user:', err)
      setError(err.response?.data?.message || 'Failed to save staff user.')
    }
  }

  const handleDeleteUser = async (username) => {
    const user = staff[username]

    if (!user?.id) {
      return
    }

    try {
      await axios.delete(`${BASE_URL}/users/${user.id}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      setStaff(prev => {
        const updated = { ...prev }
        delete updated[username]
        return updated
      })

      setError('')
    } catch (err) {
      console.error('Failed to delete staff user:', err)
      setError(err.response?.data?.message || 'Failed to delete staff user.')
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
                {Object.entries(staff).map(([username, member]) => (
                  <tr key={username}>
                    <td>{username}</td>
                    <td>{member.name}</td>
                    <td>{member.email}</td>
                    <td>
                      <button id='edit-button' onClick={() => handleEditClick(username)}>
                        Edit
                      </button>
                    </td>
                    <td>
                      <button className='delete' onClick={() => handleDeleteUser(username)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div id='add-staff-button-container'>
            <button id='add-staff-button' onClick={handleAddClick}>+</button>
          </div>
        </div>
      </div>

      {showModal && (
        <UserModal
          initialData={selectedUser}
          onClose={() => setShowModal(false)}
          onSave={handleSaveUser}
        />
      )}
    </div>
  )
}

export default Staff