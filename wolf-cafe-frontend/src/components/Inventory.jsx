import { useState, useEffect } from 'react'
import '../css/Inventory.css'
import '../css/Tables.css'
import '../css/Common.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import InventoryModal from './InventoryModal.jsx'
import { getInventory, updateInventory } from '../services/InventoryService'

function Inventory() {

  const sidebarData = {
    'Catalog': {
      'Inventory': '/inventory',
      'Recipes': '/recipes'
    },

    'Orders': {
      'Active Orders': '/active'
    }
  }

  const [showModal, setShowModal] = useState(false);

  const [inventory, setInventory] = useState([
    { ingredientName: 'Sugar', amount: 12 },
    { ingredientName: 'Coffee', amount: 25 },
    { ingredientName: 'Milk', amount: 40 },
    { ingredientName: 'Carmel', amount: 5 }
  ])

  const [error, setError ] = useState('')

  useEffect(() => {
    const fetchInventory = async () => {
      try {
        const response = await getInventory()
        const ingredients = response.data?.ingredients
        if (Array.isArray(ingredients) && ingredients.length > 0) {
          setInventory(ingredients)
        }
      } catch (error) {
        console.error(error)
        setError('Failed to load Inventory.')
      }
    }
    fetchInventory()
  }, [])

  // Sends the new list to the backend. On failure the optimistic local change
  // is rolled back so the table keeps matching what is actually stored -- the
  // backend rejects ingredient names that fail the profanity filter.
  const pushUpdate = async (updatedList, previousList) => {
    try {
      await updateInventory(updatedList)
      setError('')
    } catch( err ) {
      console.error( err )
      setError(err.response?.data?.message || 'Failed to save changes.')
      if (previousList) {
        setInventory(previousList)
      }
    }
  }

  const increment = (item) => {
    const updatedList = inventory.map(i =>
      i.ingredientName === item ? { ...i, amount: i.amount + 1 } : i
    )

    setInventory(updatedList)
    pushUpdate(updatedList, inventory)
  }

  const decrement = (item) => {
    const updatedList = inventory.map(i =>
      i.ingredientName === item ? { ...i, amount: Math.max(0, i.amount - 1) } : i
    )

    setInventory(updatedList)
    pushUpdate(updatedList, inventory)
  }

  const handleInput = (item, value) => {
    if (value === '') {
      setInventory(prev =>
        prev.map(i => i.ingredientName === item ? { ...i, amount: '' } : i)
      )
      return
    }

    const parsed = parseInt(value)
    const updatedList = inventory.map(i =>
      i.ingredientName === item ? { ...i, amount: isNaN(parsed) ? 0 : Math.max(0, parsed) } : i
    )

    setInventory(updatedList)
    pushUpdate(updatedList, inventory)
  }

  
  const handleAdd = (ingredient) => {
    console.log(ingredient)
    const updatedList = [...inventory, { ingredientName: ingredient.name, amount: ingredient.amount }]
    setInventory(updatedList)
    pushUpdate(updatedList, inventory)
  }

  const handleDelete = (item) => {
    const updatedList = inventory.filter(i => i.ingredientName !== item)
    setInventory(updatedList)
    pushUpdate(updatedList, inventory)
  }

  return (

    <div id='main'>
      <Sidebar data={sidebarData}/>
      <div id='main-right'>
        <Header />
        <div id="main-container">
          {error && <p className='error'>{error}</p>}
          <div className='table-wrap'>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Amount</th>
                  <th>&nbsp;</th>
                  <th>&nbsp;</th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((item) => (
                  <tr key={item.ingredientName}>
                    <td>{item.ingredientName}</td>
                    <td>
                      <input
                        type='number'
                        value={item.amount}
                        onChange={(e) => handleInput(item.ingredientName, e.target.value)}
                        min='0'
                      />
                    </td>
                    <td>
                      <button onClick={() => increment(item.ingredientName)}>+</button>
                      <button onClick={() => decrement(item.ingredientName)}>-</button>
                    </td>
                    <td>
                      <button className='delete' id='delete-button' onClick={() => handleDelete(item.ingredientName)}>
                        x
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div id='add-ingredient-button-container'>
            <button id="add-ingredient-button" onClick={() => setShowModal(true)}>+</button>
        </div>
    </div>
      </div>

      {showModal && (
        <InventoryModal
          onClose={() => setShowModal(false)}
          onAdd={handleAdd}
        />
      )}
      
    </div>
    
  )
}

export default Inventory
