import '../css/App.css'
import Login from './Login.jsx'
import Inventory from './Inventory.jsx'
import Recipes from './Recipes.jsx'
import Menu from './Menu.jsx'
import Register from './Register.jsx'
import CurrentOrders from './CurrentOrders.jsx'
import OrderHistory from './OrderHistory.jsx'
import Pay from './Pay.jsx'
import Tax from './Tax.jsx'
import Staff from './Staff.jsx'
import Customer from './Customer.jsx'
import ActiveOrders from './ActiveOrders.jsx'

import { Routes, Route, Navigate } from 'react-router-dom'


function App() {
  return (
    <>
      <div id="app-background">
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/inventory" element={<Inventory />} />
          <Route path="/recipes" element={<Recipes />} />
          <Route path="/menu" element={<Menu />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/current" element={<CurrentOrders />} />
          <Route path="/history" element={<OrderHistory />} />
          <Route path="/pay" element={<Pay />} />
          <Route path="/tax" element={<Tax />} />
          <Route path="/staff" element={<Staff />} />
          <Route path="/customer" element={<Customer />} />
          <Route path="/active" element={<ActiveOrders />} />
        </Routes>
      </div>
      
    </>
    
  )
}

export default App
