import React, { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className='flex flex-col items-center justify-center min-h-screen gap-4 bg-background p-6'>
          <h1 className='text-3xl font-bold text-destructive'>Something went wrong</h1>
          <p className='text-muted-foreground text-center max-w-md'>{this.state.error?.message}</p>
          <button
            onClick={() => this.setState({ hasError: false, error: null })}
            className='px-6 py-3 bg-primary text-primary-foreground rounded-2xl hover:opacity-90 transition-opacity font-bold shadow-xl shadow-primary/20'
          >
            Try again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
