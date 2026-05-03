export const Input = ({
  type = 'text',
  placeholder = '',
  className = '',
  disabled = false,
  error = false,
  ...props
}) => {
  const baseStyles = 'w-full px-3 py-2 border rounded font-medium transition-colors duration-200';
  const borderStyles = error
    ? 'border-red-500 focus:outline-none focus:ring-2 focus:ring-red-200'
    : 'border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-200';

  return (
    <input
      type={type}
      placeholder={placeholder}
      disabled={disabled}
      className={`${baseStyles} ${borderStyles} ${disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'} ${className}`}
      {...props}
    />
  );
};

export default Input;
