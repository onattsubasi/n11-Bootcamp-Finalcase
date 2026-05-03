import { Component } from 'react';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className='flex flex-col items-center justify-center min-h-screen gap-4'>
          <h1 className='text-2xl font-bold text-red-600'>Something went wrong</h1>
          <p className='text-gray-600'>{this.state.error?.message}</p>
          <button
            onClick={() => this.setState({ hasError: false, error: null })}
            className='px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700'
          >
            Try again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
