import { create } from 'zustand';

import { SubareaData } from "../types/subareas";

interface SubareaCacheStore {
  // Cache by subareaId
  cache: Record<string, SubareaData>;
  
  // Loading states by subareaId
  loading: Record<string, boolean>;
  
  // Error states by subareaId
  errors: Record<string, string | null>;
  
  // Actions
  setSubareaData: (subareaId: string, data: SubareaData) => void;
  setLoading: (subareaId: string, loading: boolean) => void;
  setError: (subareaId: string, error: string | null) => void;
  clearCache: (subareaId?: string) => void;
  getSubareaData: (subareaId: string) => SubareaData | null;
  isCached: (subareaId: string) => boolean;
}

export const useSubareaCache = create<SubareaCacheStore>((set, get) => ({
  cache: {},
  loading: {},
  errors: {},
  
  setSubareaData: (subareaId: string, data: SubareaData) => {
    set((state) => ({
      cache: {
        ...state.cache,
        [subareaId]: data
      },
      loading: {
        ...state.loading,
        [subareaId]: false
      },
      errors: {
        ...state.errors,
        [subareaId]: null
      }
    }));
  },
  
  setLoading: (subareaId: string, loading: boolean) => {
    set((state) => ({
      loading: {
        ...state.loading,
        [subareaId]: loading
      }
    }));
  },
  
  setError: (subareaId: string, error: string | null) => {
    set((state) => ({
      errors: {
        ...state.errors,
        [subareaId]: error
      },
      loading: {
        ...state.loading,
        [subareaId]: false
      }
    }));
  },
  
  clearCache: (subareaId?: string) => {
    if (subareaId) {
      set((state) => {
        const newCache = { ...state.cache };
        const newLoading = { ...state.loading };
        const newErrors = { ...state.errors };
        
        delete newCache[subareaId];
        delete newLoading[subareaId];
        delete newErrors[subareaId];
        
        return {
          cache: newCache,
          loading: newLoading,
          errors: newErrors
        };
      });
    } else {
      set({
        cache: {},
        loading: {},
        errors: {}
      });
    }
  },
  
  getSubareaData: (subareaId: string) => {
    return get().cache[subareaId] || null;
  },
  
  isCached: (subareaId: string) => {
    return !!get().cache[subareaId];
  }
})); 