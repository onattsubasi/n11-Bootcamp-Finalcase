import React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AppRouter } from '@/router';
import { queryClient } from './lib/queryClient';
import { ErrorBoundary } from '@/app/ErrorBoundary';

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <AppRouter />
      </ErrorBoundary>
      <Toaster 
        position="bottom-right" 
        toastOptions={{
          style: {
            borderRadius: '20px',
            background: '#111827',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 'bold',
            padding: '16px 24px',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)'
          }
        }} 
      />
    </QueryClientProvider>
  );
}

export default App;
