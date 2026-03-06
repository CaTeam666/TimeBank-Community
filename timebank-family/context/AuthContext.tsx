import React, { createContext, useContext, useReducer, ReactNode, useEffect } from 'react';
import { User, FamilyMember, AuthState, AuthAction, UserRole } from '../types';
import { MOCK_FAMILY_MEMBERS } from '../constants';

const storedToken = localStorage.getItem('token');
let storedUser = null;
try {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    storedUser = JSON.parse(userStr);
  }
} catch (error) {
  console.error("Failed to parse stored user", error);
  localStorage.removeItem('user');
}

const initialState: AuthState = {
  currentUser: storedUser,
  isAuthenticated: !!storedToken && !!storedUser,
  isProxyMode: false,
  proxyTarget: null,
  familyMembers: MOCK_FAMILY_MEMBERS,
};

const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'LOGIN':
      return {
        ...state,
        isAuthenticated: true,
        currentUser: action.payload,
      };
    case 'LOGOUT':
      localStorage.removeItem('token');
      // user removal handled by effect
      return {
        ...initialState,
        currentUser: null,
        isAuthenticated: false,
        isProxyMode: false,
        proxyTarget: null
      };
    case 'UPDATE_BALANCE':
      const { userId, amount } = action.payload;

      // Check if it's current user
      if (state.currentUser && state.currentUser.id === userId) {
        return {
          ...state,
          currentUser: { ...state.currentUser, balance: amount }
        };
      }

      // Check if it's a family member
      const familyIndex = state.familyMembers.findIndex(m => m.id === userId);
      if (familyIndex !== -1) {
        const updatedFamily = [...state.familyMembers];
        updatedFamily[familyIndex] = { ...updatedFamily[familyIndex], balance: amount };

        // Also update proxyTarget if it's the same person
        let updatedProxyTarget = state.proxyTarget;
        if (state.proxyTarget && state.proxyTarget.id === userId) {
          updatedProxyTarget = { ...state.proxyTarget, balance: amount };
        }

        return {
          ...state,
          familyMembers: updatedFamily,
          proxyTarget: updatedProxyTarget
        };
      }
      return state;

    case 'TOGGLE_PROXY':
      return {
        ...state,
        isProxyMode: !!action.payload,
        proxyTarget: action.payload,
      };
    case 'ADD_FAMILY_MEMBER':
      return {
        ...state,
        familyMembers: [...state.familyMembers, action.payload]
      };
    case 'REMOVE_FAMILY_MEMBER':
      return {
        ...state,
        familyMembers: state.familyMembers.filter(m => m.id !== action.payload)
      };
    default:
      return state;
  }
};

const AuthContext = createContext<{
  state: AuthState;
  dispatch: React.Dispatch<AuthAction>;
} | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Sync currentUser with localStorage
  useEffect(() => {
    if (state.currentUser) {
      localStorage.setItem('user', JSON.stringify(state.currentUser));
    } else {
      localStorage.removeItem('user');
    }
  }, [state.currentUser]);

  return (
    <AuthContext.Provider value={{ state, dispatch }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
