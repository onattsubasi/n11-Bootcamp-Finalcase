import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useLogin } from '../features/auth/hooks/useLogin';
import { useStore } from '../store';

const Login = () => {
  const { mutate: login, isPending, isError } = useLogin();
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const location = useLocation();
  const navigate = useNavigate();
  const from = location.state?.from?.pathname || '/';

  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    login(Object.fromEntries(formData), {
      onSuccess: () => navigate(from, { replace: true }),
    });
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>
      {isError ? <p className="text-red-500 mb-4">Login failed. Please try again.</p> : null}
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <input 
          type="email" 
          name="email" 
          placeholder="Email" 
          required 
          className="border p-2 rounded"
        />
        <input 
          type="password" 
          name="password" 
          placeholder="Password" 
          required 
          className="border p-2 rounded"
        />
        <button 
          type="submit" 
          disabled={isPending}
          className="bg-blue-600 text-white p-2 rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {isPending ? 'Logging in...' : 'Login'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm">
        Don't have an account?{' '}
        <Link to="/register" className="text-blue-600 hover:underline">
          Register
        </Link>
      </p>
    </div>
  );
};

export default Login;
