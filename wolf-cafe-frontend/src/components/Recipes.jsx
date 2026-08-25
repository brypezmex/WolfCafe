import { useState, useEffect } from 'react'
import '../css/Common.css'
import '../css/Tables.css'
import '../css/Recipes.css'
import Sidebar from './Sidebar.jsx'
import Header from './Header.jsx'
import RecipesModal from './RecipesModal.jsx'
import { getInventory } from '../services/InventoryService'
import {
  saveRecipe,
  getAllRecipes,
  updateRecipe,
  deleteRecipeById
} from '../services/RecipeService'

function Recipes() {
  const sidebarData = {
    Catalog: {
      Inventory: '/inventory',
      Recipes: '/recipes'
    },
    Orders: {
      'Active Orders': '/active'
    }
  }

  const [recipes, setRecipes] = useState([])
  const [inventoryNames, setInventoryNames] = useState([])
  const [showModal, setShowModal] = useState(false)
  const [editingRecipe, setEditingRecipe] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    loadRecipes()
    loadInventoryNames()
  }, [])

  const loadRecipes = async () => {
    try {
      const response = await getAllRecipes()
      setRecipes(response.data)
    } catch (err) {
      console.error('Failed to fetch recipes:', err)
      setError('Failed to load recipes.')
    }
  }

  const loadInventoryNames = async () => {
    try {
      const response = await getInventory()
      const ingredients = response.data?.ingredients || []
      setInventoryNames(ingredients.map(i => i.ingredientName))
    } catch (err) {
      console.error('Failed to load inventory:', err)
    }
  }

  const handleOpenAdd = () => {
    setEditingRecipe(null)
    setShowModal(true)
    setError('')
  }

  const handleEdit = (recipe) => {
    setEditingRecipe(recipe)
    setShowModal(true)
    setError('')
  }

  const handleDelete = async (id) => {
    try {
      await deleteRecipeById(id)
      setRecipes(prev => prev.filter(r => r.id !== id))
    } catch (err) {
      console.error('Failed to delete recipe:', err)
      setError('Failed to delete recipe.')
    }
  }

  const handleSave = async (modalData) => {
    setError('')

    const ingredientsArray = Object.entries(modalData.ingredients).map(([ingredientName, amount]) => ({
      ingredientName,
      amount
    }))

    const recipePayload = {
      name: modalData.name,
      price: modalData.price,
      ingredients: ingredientsArray
    }

    try {
      if (editingRecipe) {
        const response = await updateRecipe(editingRecipe.id, {
          id: editingRecipe.id,
          ...recipePayload
        })
        setRecipes(prev => prev.map(r => r.id === editingRecipe.id ? response.data : r))
      } else {
        const response = await saveRecipe(recipePayload)
        setRecipes(prev => [...prev, response.data])
      }
      // The modal shows its own confirmation and closes itself via onClose,
      // which is what makes its "Add another" flow reachable.
    } catch (err) {
      console.error('Failed to save recipe:', err)
      if (err.response?.status === 409) {
        setError('A recipe with that name already exists.')
      } else if (err.response?.status === 507) {
        setError('The maximum number of recipes has been reached.')
      } else {
        // The backend explains 400s itself (duplicate name, profanity filter).
        setError(err.response?.data?.message || 'Failed to save recipe.')
      }
      // Rethrow so the modal stays open, keeps the user's input, and can show
      // the reason inline instead of flashing a success screen.
      throw err
    }
  }

  const recipeToModalFormat = (recipe) => {
    if (!recipe) return null
    const ingredients = {}
    ;(recipe.ingredients || []).forEach(ing => {
      ingredients[ing.ingredientName] = ing.amount
    })
    return { ...recipe, ingredients }
  }

  return (
    <div id='main'>
      <Sidebar data={sidebarData} />
      <div id='main-right'>
        <Header />
        <div id='main-container'>
          {error && <p className='page-error'>{error}</p>}
          <div className='table-wrap'>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Price</th>
                  <th>Ingredients</th>
                  <th>Edit</th>
                  <th>Delete</th>
                </tr>
              </thead>
              <tbody>
                {recipes.map(recipe => (
                  <tr key={recipe.id}>
                    <td>{recipe.name}</td>
                    <td>${Number(recipe.price).toFixed(2)}</td>
                    <td>
                      {(recipe.ingredients || []).map(ingredient => (
                        <div key={ingredient.id ?? ingredient.ingredientName}>
                          {ingredient.amount}× {ingredient.ingredientName}
                        </div>
                      ))}
                    </td>
                    <td>
                      <button id='edit-button' onClick={() => handleEdit(recipe)}>
                        Edit
                      </button>
                    </td>
                    <td>
                      <button className='delete' id='delete-button' onClick={() => handleDelete(recipe.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {recipes.length === 0 && (
                  <tr>
                    <td colSpan='5'>No recipes yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div id='add-recipe-button-container'>
            <button id='add-recipe-button' onClick={handleOpenAdd}>+</button>
          </div>
        </div>
      </div>

      {showModal && (
        <RecipesModal
          onClose={() => { setShowModal(false); setEditingRecipe(null) }}
          onSave={handleSave}
          ingredients={inventoryNames}
          editingRecipe={recipeToModalFormat(editingRecipe)}
        />
      )}
    </div>
  )
}

export default Recipes