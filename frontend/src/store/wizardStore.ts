import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Area } from '@/types/areas';
import { slugify } from '@/utils/slugify';
import { v4 as uuidv4 } from 'uuid';

interface WizardState {
  // ... existing state
  areas: Area[];

  // Areas actions
  addArea: (area: Omit<Area, 'id' | 'createdAt'>) => void;
  updateArea: (id: string, updates: Partial<Area>) => void;
  deleteArea: (id: string) => void;
  getDefaultArea: () => Area | null;
  canAddMoreAreas: () => boolean;
}

const MAX_AREAS = 5;

const createDefaultArea = (): Area => ({
  id: uuidv4(),
  code: 'default',
  name: 'Default Area',
  description: 'This is the default area grouping all subareas.',
  isDefault: true,
  createdAt: new Date(),
});

export const useWizardStore = create<WizardState>()(
  persist(
    (set, get) => ({
      // ... existing state
      areas: [],

      addArea: (area) => set((state) => {
        if (state.areas.length >= MAX_AREAS) return {};
        const code = slugify(area.name);
        const isDefault = false;
        const newArea: Area = {
          ...area,
          id: uuidv4(),
          code,
          isDefault,
          createdAt: new Date(),
        };
        return { areas: [...state.areas, newArea] };
      }),

      updateArea: (id, updates) => set((state) => ({
        areas: state.areas.map((area) =>
          area.id === id
            ? { ...area, ...updates, code: updates.name ? slugify(updates.name) : area.code }
            : area
        ),
      })),

      deleteArea: (id) => set((state) => {
        const filtered = state.areas.filter((area) => area.id !== id);
        return { areas: filtered.length === 0 ? [createDefaultArea()] : filtered };
      }),

      getDefaultArea: () => {
        const { areas } = get();
        return areas.find((a) => a.isDefault) || null;
      },

      canAddMoreAreas: () => {
        const { areas } = get();
        return areas.length < MAX_AREAS;
      },
    }),
    {
      name: 'wizard-storage',
    }
  )
); 