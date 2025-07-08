"use client";
import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export type User = {
  id: number;
  username: string;
  email: string;
  role: "ADMIN" | "MANAGER" | "USER";
};

type AuthContextType = {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: string | null;
  refresh: () => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUser = async () => {
    console.log('Fetching user...');
    setIsLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/me`, { credentials: "include" });
      console.log('Response status:', res.status);
      if (res.ok) {
        const data = await res.json();
        console.log('User data:', data);
        setUser(data);
      } else {
        console.log('Failed to fetch user, status:', res.status);
        setUser(null);
      }
    } catch (err) {
      console.log('Error fetching user:', err);
      setUser(null);
      setError("Failed to fetch user");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const logout = async () => {
    await fetch(`${API_BASE}/auth/logout`, { method: "POST", credentials: "include" });
    setUser(null);
  };

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    error,
    refresh: fetchUser,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
} 