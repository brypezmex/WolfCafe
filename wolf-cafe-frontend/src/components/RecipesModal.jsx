import { useState } from "react";
import '../css/Modal.css';

export default function RecipesModal({ onClose, onSave, ingredients = [], editingRecipe }) {
  const isEditing = !!editingRecipe;

  const [name, setName] = useState(editingRecipe?.name ?? "");
  const [price, setPrice] = useState(editingRecipe?.price?.toString() ?? "");
  const [selectedIng, setSelectedIng] = useState("");
  const [ingAmt, setIngAmt] = useState("");
  const [addedIngs, setAddedIngs] = useState(
    editingRecipe?.ingredients && typeof editingRecipe.ingredients === 'object' && !Array.isArray(editingRecipe.ingredients)
      ? editingRecipe.ingredients
      : {}
  );
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);
  const [saving, setSaving] = useState(false);

  const removeIng = (ing) => {
    setAddedIngs(prev => { const n = { ...prev }; delete n[ing]; return n; });
  };

  const handleAddIng = () => {
    const amt = parseInt(ingAmt);
    if (!selectedIng) return setErrors(p => ({ ...p, ingRow: "Please select an ingredient." }));
    if (!amt || amt < 1) return setErrors(p => ({ ...p, ingRow: "Please enter a valid quantity." }));
    setAddedIngs(prev => ({ ...prev, [selectedIng]: amt }));
    setSelectedIng(""); setIngAmt("");
    setErrors(p => ({ ...p, ingRow: null }));
  };

  const handleCreate = async () => {
    const e = {};
    if (!name.trim()) e.name = "Please enter a recipe name.";
    if (!price || parseFloat(price) <= 0) e.price = "Please enter a valid price.";
    if (Object.keys(addedIngs).length === 0) e.ingredients = "Please add at least one ingredient.";
    if (Object.keys(e).length) return setErrors(e);

    setSaving(true);
    try {
      // Wait for the save to come back before claiming success: the backend
      // rejects duplicate names and text that fails the profanity filter, and
      // the user needs to see why.
      await onSave?.({
        id: editingRecipe?.id,
        name: name.trim(),
        price: parseFloat(parseFloat(price).toFixed(2)),
        ingredients: { ...addedIngs }
      });
      setSuccess(true);
    } catch (err) {
      setErrors({ submit: err?.response?.data?.message || 'Failed to save recipe.' });
    } finally {
      setSaving(false);
    }
  };

  const reset = () => {
    setName(""); setPrice(""); setSelectedIng(""); setIngAmt("");
    setAddedIngs({}); setErrors({}); setSuccess(false); setSaving(false);
    onClose?.();
  };

  return (
    <div className="modal-backdrop" onClick={reset}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{isEditing ? 'Edit recipe' : 'Create recipe'}</h2>
          <button onClick={reset}>✕</button>
        </div>

        {!success ? (
          <>
            <div className="field">
              <label>Recipe name</label>
              <input type="text" placeholder="e.g. Mocha Latte" value={name}
                onChange={e => { setName(e.target.value); setErrors(p => ({ ...p, name: null, submit: null })); }} />
              {errors.name && <span className="error">{errors.name}</span>}
            </div>

            <div className="field">
              <label>Ingredients</label>
              <div className="chips-container">
                {Object.keys(addedIngs).length === 0
                  ? <span style={{ fontSize: '13px', color: '#aaa', alignSelf: 'center' }}>No ingredients added yet</span>
                  : Object.entries(addedIngs).map(([ing, qty]) => (
                    <div key={ing} className="chip">
                      {ing} | {qty}
                      <button onClick={() => removeIng(ing)}>✕</button>
                    </div>
                  ))
                }
              </div>
              {errors.ingredients && <span className="error">{errors.ingredients}</span>}
              <div className="add-ingredient-row">
                <select value={selectedIng} onChange={e => { setSelectedIng(e.target.value); setErrors(p => ({ ...p, ingRow: null })); }}>
                  <option value="">Select ingredient</option>
                  {ingredients.map(i => <option key={i} value={i}>{i}</option>)}
                </select>
                <input type="number" placeholder="Qty" min="1" value={ingAmt}
                  onChange={e => { setIngAmt(e.target.value); setErrors(p => ({ ...p, ingRow: null })); }} />
                <button onClick={handleAddIng}>+ Add</button>
              </div>
              {errors.ingRow && <span className="error">{errors.ingRow}</span>}
            </div>

            <div className="field">
              <label>Price ($)</label>
              <input type="number" placeholder="e.g. 4.99" min="0" step="0.01" value={price}
                onChange={e => { setPrice(e.target.value); setErrors(p => ({ ...p, price: null })); }} />
              {errors.price && <span className="error">{errors.price}</span>}
            </div>

            {errors.submit && <span className="error">{errors.submit}</span>}

            <button onClick={handleCreate} disabled={saving}>
              {saving ? 'Saving...' : (isEditing ? 'Save changes' : 'Create')}
            </button>
          </>
        ) : (
          <div className="success">
            <div className="check">✓</div>
            <p>{name}</p>
            <span>{isEditing ? 'Recipe updated successfully.' : 'Recipe created successfully.'}</span>
            {!isEditing && (
              <button onClick={() => { setSuccess(false); setName(""); setPrice(""); setAddedIngs({}); }}>
                Add another
              </button>
            )}
            <button onClick={reset}>Done</button>
          </div>
        )}
      </div>
    </div>
  );
}