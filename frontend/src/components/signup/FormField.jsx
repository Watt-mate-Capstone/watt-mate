import React from 'react';

export default function FormField({
  label,
  name,
  value,
  onChange,
  type = 'text',
  placeholder,
  full = false,
  children,
  ...props
}) {
  return (
    <div className={`signupform-group${full ? ' full' : ''}`}>
      <label>{label}</label>
      {children || (
        <input
          type={type}
          name={name}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          {...props}
        />
      )}
    </div>
  );
}
