import React from 'react';
import { TabBar } from './UIComponents';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="max-w-md mx-auto bg-white min-h-screen shadow-2xl relative">
      {children}
      <TabBar />
    </div>
  );
};
