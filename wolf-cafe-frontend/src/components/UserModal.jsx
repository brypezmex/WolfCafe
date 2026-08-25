import { useState, useEffect } from "react";
import '../css/Modal.css';

export default function UserModal({ onClose, onSave, initialData = null }) {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    username: "",
    password: "",
    confirmPassword: ""
  });

  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);

  // Determine if we are editing or creating
  const isEditing = !!initialData;

  // Populate form if initialData is provided (Editing mode)
  useEffect(() => {
    if (initialData) {
      setFormData({
        ...initialData,
        password: "", // Keep passwords blank for security/clarity
        confirmPassword: ""
      });
    }
  }, [initialData]);

  const validate = () => {
    const e = {};
    if (!formData.name.trim()) e.name = "Name is required.";
    if (!formData.username.trim()) e.username = "Username is required.";
    
    // Simple Email Regex
    if (!/^\S+@\S+\.\S+$/.test(formData.email)) e.email = "Enter a valid email.";

    // Password validation logic
    if (!isEditing && !formData.password) {
      e.password = "Password is required.";
    }
    if (formData.password !== formData.confirmPassword) {
      e.confirmPassword = "Passwords do not match.";
    }
    
    return e;
  };

  const handleSave = () => {
    const e = validate();
    if (Object.keys(e).length) return setErrors(e);

    onSave?.(formData);
    setSuccess(true);
  };

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: null }));
  };

  const reset = () => {
    setFormData({ name: "", email: "", username: "", password: "", confirmPassword: "" });
    setErrors({});
    setSuccess(false);
    onClose?.();
  };

  return (
    <div className="modal-backdrop" onClick={reset}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{isEditing ? "Edit User" : "Create User"}</h2>
          <button onClick={reset}>✕</button>
        </div>

        {!success ? (
          <div className="modal-body">
            <div className="field">
              <label>Full Name</label>
              <input
                type="text"
                value={formData.name}
                onChange={e => handleChange("name", e.target.value)}
              />
              {errors.name && <span className="error">{errors.name}</span>}
            </div>

            <div className="field">
              <label>Email</label>
              <input
                type="email"
                value={formData.email}
                onChange={e => handleChange("email", e.target.value)}
              />
              {errors.email && <span className="error">{errors.email}</span>}
            </div>

            <div className="field">
              <label>Username</label>
              <input
                type="text"
                value={formData.username}
                onChange={e => handleChange("username", e.target.value)}
              />
              {errors.username && <span className="error">{errors.username}</span>}
            </div>

            <div className="field">
              <label>{isEditing ? "New Password (optional)" : "Password"}</label>
              <input
                type="password"
                value={formData.password}
                onChange={e => handleChange("password", e.target.value)}
              />
              {errors.password && <span className="error">{errors.password}</span>}
            </div>

            <div className="field">
              <label>Confirm Password</label>
              <input
                type="password"
                value={formData.confirmPassword}
                onChange={e => handleChange("confirmPassword", e.target.value)}
              />
              {errors.confirmPassword && <span className="error">{errors.confirmPassword}</span>}
            </div>

            <button className="save-btn" onClick={handleSave}>
              {isEditing ? "Update User" : "Create User"}
            </button>
          </div>
        ) : (
          <div className="success">
            <div className="check">✓</div>
            <p>{formData.username} has been saved.</p>
            <button onClick={reset}>Close</button>
          </div>
        )}
      </div>
    </div>
  );
}