"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

interface DashboardEditModeContextType {
  isEditMode: boolean;
  toggleEditMode: () => void;
  setEditMode: (mode: boolean) => void;
}

const DashboardEditModeContext = createContext<DashboardEditModeContextType | undefined>(undefined);

export function DashboardEditModeProvider({ children }: { children: ReactNode }) {
  const [isEditMode, setIsEditMode] = useState(false);

  const toggleEditMode = () => {
    setIsEditMode(!isEditMode);
  };

  const setEditMode = (mode: boolean) => {
    setIsEditMode(mode);
  };

  const value: DashboardEditModeContextType = {
    isEditMode,
    toggleEditMode,
    setEditMode,
  };

  return (
    <DashboardEditModeContext.Provider value={value}>
      {children}
    </DashboardEditModeContext.Provider>
  );
}

export function useDashboardEditMode() {
  const context = useContext(DashboardEditModeContext);
  if (context === undefined) {
    throw new Error('useDashboardEditMode must be used within a DashboardEditModeProvider');
  }
  return context;
} 