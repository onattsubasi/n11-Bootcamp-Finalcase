import { useNavigate, Link } from 'react-router-dom';
import { useRegister } from '../features/auth/hooks/useRegister';

const Register = () => {
  const { mutate: register, isPending, isError } = useRegister();
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const payload = Object.fromEntries(formData);

    if (payload.password !== payload.confirmPassword) {
      return;
    }

    register(
      {
        email: payload.email,
        password: payload.password,
        fullName: payload.fullName,
      },
      {
        onSuccess: (data) => {
          if (data?.data?.accessToken || data?.accessToken || data?.token) {
            navigate('/', { replace: true });
            return;
          }

          navigate('/login', { replace: true });
        },
      }
    );
  };

  return (
    <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded shadow-md">
      <h2 className="text-2xl font-bold mb-6 text-center">Register</h2>
      {isError ? <p className="text-red-500 mb-4">Register failed. Please try again.</p> : null}
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <input type="text" name="fullName" placeholder="Full Name" required className="border p-2 rounded" />
        <input type="email" name="email" placeholder="Email" required className="border p-2 rounded" />
        <input type="password" name="password" placeholder="Password" required className="border p-2 rounded" />
        <input
          type="password"
          name="confirmPassword"
          placeholder="Confirm Password"
          required
          className="border p-2 rounded"
        />
        <button
          type="submit"
          disabled={isPending}
          className="bg-blue-600 text-white p-2 rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {isPending ? 'Registering...' : 'Register'}
        </button>
      </form>
      <p className="mt-4 text-center text-sm">
        Already have an account?{' '}
        <Link className="text-blue-600 hover:underline" to="/login">
          Login
        </Link>
      </p>
    </div>
  );
};

export default Register;
