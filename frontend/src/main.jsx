import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import { cleanupLegacyAuthStorage } from './store/cleanupLegacyAuthStorage';

cleanupLegacyAuthStorage();

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
