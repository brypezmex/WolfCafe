 import "../css/MenuNode.css"

function MenuNode({ name, price, ingredients, pic, count, onAdd, onRemove }) {
  return (
    <div className='menuNode-container'>
      <div id='menuNode-top'>
        <img src={pic} alt={name} />
      </div>
      <div className='menuNode-bottom'>
        <div className='item-info'>
          <p className='item-name'>{name}</p>
          <p className='item-price'>${Number(price).toFixed(2)}</p>
        </div>
        <div className='item-amount'>
          <button className='item-remove' onClick={onRemove}>-</button>
          <p className='item-count'>{count}</p>
          <button className='item-add' onClick={onAdd}>+</button>
        </div>
      </div>
    </div>
  )
}

export default MenuNode