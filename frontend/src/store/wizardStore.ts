import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Area, AreaFormData } from '@/types/areas';
import { slugify } from '@/utils/slugify';
import { v4 as uuidv4 } from 'uuid';
import { Subarea, SubareaFormData } from '@/types/subareas';
import * as areaService from '@/services/areaService';

interface WizardState {
  areas: Area[];
  subareas: Subarea[];

  // Areas actions
  setAreas: (areas: Area[]) => void;
  fetchAreas: () => Promise<void>;
  addArea: (area: AreaFormData) => Promise<void>;
  updateArea: (id: string, updates: Partial<AreaFormData>) => Promise<void>;
  deleteArea: (id: string) => Promise<void>;
  getDefaultArea: () => Area | null;
  canAddMoreAreas: () => boolean;

  // Subareas actions
  addSubarea: (subarea: Omit<Subarea, 'id' | 'createdAt'>) => void;
  updateSubarea: (id: string, updates: Partial<Subarea>) => void;
  deleteSubarea: (id: string) => void;
  getSubareasByAreaId: (areaId: string) => Subarea[];
  getDefaultAreaId: () => string | null;

  setSubareas: (subs: Subarea[]) => void;
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
      areas: [],
      subareas: [],

      setAreas: (areas) => set({ areas }),

      fetchAreas: async () => {
        try {
          const areas = await areaService.getAreas();
          if (areas.length === 0) {
            set({ areas: [createDefaultArea()] });
          } else {
            set({ areas });
          }
        } catch (error) {
          console.error("Failed to fetch areas:", error);
          set({ areas: [createDefaultArea()] });
        }
      },

      addArea: async (areaForm) => {
        if (get().areas.filter(a => !a.isDefault).length >= MAX_AREAS) {
            throw new Error(`You can only add up to ${MAX_AREAS} areas.`);
        }
        const newArea = await areaService.createArea(areaForm);
        set((state) => {
            const existingUserAreas = state.areas.filter(a => !a.isDefault);
            return { areas: [...existingUserAreas, newArea] };
        });
      },

      updateArea: async (id, updates) => {
          const updatedArea = await areaService.updateArea(id, updates);
          set((state) => ({
            areas: state.areas.map((area) =>
              area.id === id ? updatedArea : area
            ),
          }));
      },

      deleteArea: async (id) => {
        await areaService.deleteArea(id);
        set((state) => {
          const filtered = state.areas.filter((area) => area.id !== id);
          if (filtered.filter(a => !a.isDefault).length === 0) {
            return { areas: [createDefaultArea()] };
          }
          return { areas: filtered };
        });
      },

      getDefaultArea: () => {
        const { areas } = get();
        return areas.find((a) => a.isDefault) || null;
      },

      canAddMoreAreas: () => {
        const { areas } = get();
        return areas.filter(a => !a.isDefault).length < MAX_AREAS;
      },

      addSubarea: (subarea) => set((state) => {
        const id = crypto.randomUUID();
        const createdAt = new Date();
        return { subareas: [...state.subareas, { ...subarea, id, createdAt }] };
      }),

      updateSubarea: (id, updates) => set((state) => ({
        subareas: state.subareas.map((s) => (s.id === id ? { ...s, ...updates } : s)),
      })),

      deleteSubarea: (id) => set((state) => ({
        subareas: state.subareas.filter((s) => s.id !== id),
      })),

      getSubareasByAreaId: (areaId) => get().subareas.filter((s) => s.areaId === areaId),

      getDefaultAreaId: () => {
        const areas = get().areas;
        const defaultArea = areas.find((a) => a.isDefault);
        return defaultArea ? defaultArea.id : null;
      },

      setSubareas: (subs) => set({ subareas: subs }),
    }),
    {
      name: 'wizard-storage',
    }
  )
); 