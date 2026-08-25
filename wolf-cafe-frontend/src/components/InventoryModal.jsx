import { useState } from "react";
import '../css/Modal.css'

export default function InventoryModal({ onClose, onAdd }) {
  const [name, setName] = useState("");
  const [amount, setAmount] = useState("");
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);

  const validate = () => {
    const e = {};
    if (!name.trim()) e.name = "Please enter an ingredient name.";
    if (!amount || parseFloat(amount) <= 0) e.amount = "Please enter a valid amount.";
    return e;
  };

  const handleAdd = () => {
    const e = validate();
    if (Object.keys(e).length) return setErrors(e);
    onAdd?.({ name: name.trim(), amount: parseFloat(amount) });
    setSuccess(true);
  };

  const reset = () => {
    setName(""); setAmount("");
    setErrors({}); setSuccess(false);
    onClose?.();
  };

  return (
    <div className="modal-backdrop" onClick={reset}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add ingredient</h2>
          <button onClick={reset}>✕</button>
        </div>

        {!success ? (
          <>
            <div className="field">
              <label>Name</label>
              <input
                type="text"
                placeholder="e.g. flour"
                value={name}
                onChange={e => { setName(e.target.value); setErrors(p => ({ ...p, name: null })); }}
              />
              {errors.name && <span className="error">{errors.name}</span>}
            </div>

            <div className="field">
              <label>Amount</label>
              <input
                type="number"
                placeholder="0"
                min="0"
                value={amount}
                onChange={e => { setAmount(e.target.value); setErrors(p => ({ ...p, amount: null })); }}
              />
              {errors.amount && <span className="error">{errors.amount}</span>}
            </div>

            <button onClick={handleAdd}>Add</button>
          </>
        ) : (
          <div className="success">
            <div className="check">✓</div>
            <p>{amount} of {name}</p>
            <span>Ingredient added successfully.</span>
            <button onClick={() => { setSuccess(false); setName(""); setAmount(""); }}>
              Add another
            </button>
          </div>
        )}
      </div>
    </div>
  );
}