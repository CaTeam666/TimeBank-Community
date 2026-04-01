import React from 'react';

interface BadgeProps {
  children: React.ReactNode;
  color?: 'blue' | 'green' | 'red' | 'yellow' | 'gray' | 'orange' | 'elder-red' | 'volunteer-green' | 'child-blue';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ children, color = 'blue', className = '' }) => {
  const colorClasses: Record<string, string> = {
    blue: 'bg-blue-100 text-blue-800',
    green: 'bg-green-100 text-green-800',
    red: 'bg-red-100 text-red-800',
    yellow: 'bg-yellow-100 text-yellow-800',
    gray: 'bg-gray-100 text-gray-800',
    orange: 'bg-orange-100 text-orange-800' // Added for Elder
  };

  // Helper to map color strings effectively
  const getColorClass = (c: string) => {
      if (c === 'elder-red') return 'bg-orange-100 text-orange-800 border border-orange-200';
      if (c === 'volunteer-green') return 'bg-green-100 text-green-800 border border-green-200';
      if (c === 'child-blue') return 'bg-blue-100 text-blue-800 border border-blue-200';
      
      return colorClasses[c] || colorClasses.gray;
  }

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getColorClass(color)} ${className}`}>
      {children}
    </span>
  );
};