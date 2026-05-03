import { QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AppRouter } from './router';
import { queryClient } from './lib/queryClient';
import { ErrorBoundary } from './app/ErrorBoundary';

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <AppRouter />
      </ErrorBoundary>
      <Toaster position="bottom-right" />
    </QueryClientProvider>
  );
}

export default App;
