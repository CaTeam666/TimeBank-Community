import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { auth } from '../utils/auth';

export const RequireAuth: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const location = useLocation();
  const token = auth.getToken();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
};

