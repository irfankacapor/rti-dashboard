import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Area, AreaFormData } from '@/types/areas';
import { slugify } from '@/utils/slugify';
import { v4 as uuidv4 } from 'uuid';
import { Subarea, SubareaFormData } from '@/types/subareas';
import * as areaService from '@/services/areaService';
import * as subareaService from '@/services/subareaService';

interface WizardState {
  // Backend state (persisted)
  areas: Area[];
  subareas: Subarea[];
  
  // Local changes (not persisted to backend until saved)
  dirtyAreas: Area[];
  dirtySubareas: Subarea[];
  
  // Loading states
  isLoadingAreas: boolean;
  isLoadingSubareas: boolean;
  isSaving: boolean;

  // Areas actions
  setAreas: (areas: Area[]) => void;
  fetchAreas: () => Promise<void>;
  addArea: (area: AreaFormData) => void; // Now local only
  updateArea: (id: string, updates: Partial<AreaFormData>) => void; // Now local only
  deleteArea: (id: string) => void; // Now local only
  getDefaultArea: () => Area | null;
  canAddMoreAreas: () => boolean;
  saveAreas: () => Promise<void>; // New: save to backend
  resetAreas: () => void; // New: reset to backend state

  // Subareas actions
  setSubareas: (subs: Subarea[]) => void;
  fetchSubareas: () => Promise<void>;
  addSubarea: (subarea: SubareaFormData) => void; // Now local only
  updateSubarea: (id: string, updates: Partial<SubareaFormData>) => void; // Now local only
  deleteSubarea: (id: string) => void; // Now local only
  getSubareasByAreaId: (areaId: string) => Subarea[];
  getDefaultAreaId: () => string | null;
  saveSubareas: () => Promise<void>; // New: save to backend
  resetSubareas: () => void; // New: reset to backend state

  // Step management
  saveStep: (stepId: number) => Promise<void>; // New: save current step data
  hasUnsavedChanges: () => boolean; // New: check if there are unsaved changes
}

const MAX_AREAS = 5;

const createDefaultArea = (): Area => ({
  id: uuidv4(),
  code: 'default',
  name: 'Default Area',
  description: 'This is the default area grouping all subareas.',
  isDefault: true,
  createdAt: new Date(),
  subareaCount: 0,
});

export const useWizardStore = create<WizardState>()(
  persist(
    (set, get) => ({
      areas: [],
      subareas: [],
      dirtyAreas: [],
      dirtySubareas: [],
      isLoadingAreas: false,
      isLoadingSubareas: false,
      isSaving: false,

      setAreas: (areas) => set({ areas, dirtyAreas: areas }),

      fetchAreas: async () => {
        set({ isLoadingAreas: true });
        try {
          const areas = await areaService.getAreas();
          if (areas.length === 0) {
            const defaultArea = createDefaultArea();
            set({ areas: [defaultArea], dirtyAreas: [defaultArea] });
          } else {
            set({ areas, dirtyAreas: areas });
          }
        } catch (error) {
          console.error("Failed to fetch areas:", error);
          const defaultArea = createDefaultArea();
          set({ areas: [defaultArea], dirtyAreas: [defaultArea] });
        } finally {
          set({ isLoadingAreas: false });
        }
      },

      addArea: (areaForm) => {
        const { dirtyAreas } = get();
        if (dirtyAreas.filter(a => !a.isDefault).length >= MAX_AREAS) {
            throw new Error(`You can only add up to ${MAX_AREAS} areas.`);
        }
        const newArea: Area = {
          id: uuidv4(), // Temporary ID
          code: slugify(areaForm.name),
          name: areaForm.name,
          description: areaForm.description,
          isDefault: false,
          createdAt: new Date(),
        };
        set((state) => ({
          dirtyAreas: [...state.dirtyAreas.filter(a => !a.isDefault), newArea]
        }));
      },

      updateArea: (id, updates) => {
        set((state) => ({
          dirtyAreas: state.dirtyAreas.map((area) =>
            area.id === id ? { ...area, ...updates } : area
          ),
        }));
      },

      deleteArea: (id) => {
        set((state) => {
          const filtered = state.dirtyAreas.filter((area) => area.id !== id);
          if (filtered.filter(a => !a.isDefault).length === 0) {
            return { dirtyAreas: [createDefaultArea()] };
          }
          return { dirtyAreas: filtered };
        });
      },

      saveAreas: async () => {
        const { dirtyAreas, areas } = get();
        set({ isSaving: true });
        
        try {
          // Find new areas (areas in dirtyAreas that don't exist in backend by ID)
          const newAreas = dirtyAreas.filter(dirtyArea => 
            !areas.some(backendArea => backendArea.id === dirtyArea.id)
          );
          
          // Find updated areas (areas that exist in both but have different content)
          const updatedAreas = dirtyAreas.filter(dirtyArea => 
            areas.some(backendArea => 
              backendArea.id === dirtyArea.id && 
              (backendArea.name !== dirtyArea.name || backendArea.description !== dirtyArea.description)
            )
          );
          
          // Find deleted areas (areas that exist in backend but not in dirty state)
          // Exclude default areas from deletion since they're handled specially
          const deletedAreas = areas.filter(backendArea => 
            !backendArea.isDefault && // Don't delete default areas
            !dirtyAreas.some(dirtyArea => dirtyArea.id === backendArea.id)
          );

          // Process deletions first
          for (const area of deletedAreas) {
            await areaService.deleteArea(area.id);
          }

          // Process updates
          for (const area of updatedAreas) {
            await areaService.updateArea(area.id, {
              name: area.name,
              description: area.description
            });
          }

          // Process creations
          for (const area of newAreas) {
            await areaService.createArea({
              name: area.name,
              description: area.description
            });
          }

          // Refresh from backend to get proper IDs
          const freshAreas = await areaService.getAreas();
          if (freshAreas.length === 0) {
            const defaultArea = createDefaultArea();
            set({ areas: [defaultArea], dirtyAreas: [defaultArea] });
          } else {
            set({ areas: freshAreas, dirtyAreas: freshAreas });
          }
        } catch (error) {
          console.error("Failed to save areas:", error);
          throw error;
        } finally {
          set({ isSaving: false });
        }
      },

      resetAreas: () => {
        const { areas } = get();
        set({ dirtyAreas: areas });
      },

      getDefaultArea: () => {
        const { dirtyAreas } = get();
        return dirtyAreas.find((a) => a.isDefault) || null;
      },

      canAddMoreAreas: () => {
        const { dirtyAreas } = get();
        return dirtyAreas.filter(a => !a.isDefault).length < MAX_AREAS;
      },

      setSubareas: (subs) => set({ subareas: subs, dirtySubareas: subs }),

      fetchSubareas: async () => {
        set({ isLoadingSubareas: true });
        try {
          const subareas = await subareaService.getSubareas();
          set({ subareas, dirtySubareas: subareas });
        } catch (error) {
          console.error('Failed to fetch subareas:', error);
          set({ subareas: [], dirtySubareas: [] });
        } finally {
          set({ isLoadingSubareas: false });
        }
      },

      addSubarea: (formData) => {
        const newSubarea: Subarea = {
          id: uuidv4(), // Temporary ID
          code: slugify(formData.name),
          name: formData.name,
          description: formData.description,
          areaId: formData.areaId,
          areaName: '', // Will be populated when saved
          createdAt: new Date(),
        };
        set((state) => ({ 
          dirtySubareas: [...state.dirtySubareas, newSubarea] 
        }));
      },

      updateSubarea: (id, updates) => {
        set((state) => ({
          dirtySubareas: state.dirtySubareas.map((s) => 
            s.id === id ? { ...s, ...updates } : s
          ),
        }));
      },

      deleteSubarea: (id) => {
        set((state) => ({
          dirtySubareas: state.dirtySubareas.filter((s) => s.id !== id),
        }));
      },

      saveSubareas: async () => {
        const { dirtySubareas, subareas, dirtyAreas, areas } = get();
        set({ isSaving: true });
        
        try {
          // Create a mapping from temporary area IDs to real area IDs
          const areaIdMapping = new Map<string, string>();
          dirtyAreas.forEach(dirtyArea => {
            const backendArea = areas.find(area => 
              area.name === dirtyArea.name && 
              area.description === dirtyArea.description
            );
            if (backendArea) {
              areaIdMapping.set(dirtyArea.id, backendArea.id);
            }
          });

          // Find new subareas
          const newSubareas = dirtySubareas.filter(dirtySubarea => 
            !subareas.some(backendSubarea => backendSubarea.id === dirtySubarea.id)
          );
          
          // Find updated subareas
          const updatedSubareas = dirtySubareas.filter(dirtySubarea => 
            subareas.some(backendSubarea => 
              backendSubarea.id === dirtySubarea.id && 
              (backendSubarea.name !== dirtySubarea.name || 
               backendSubarea.description !== dirtySubarea.description ||
               backendSubarea.areaId !== dirtySubarea.areaId)
            )
          );
          
          // Find deleted subareas
          const deletedSubareas = subareas.filter(backendSubarea => 
            !dirtySubareas.some(dirtySubarea => dirtySubarea.id === backendSubarea.id)
          );

          // Process deletions first
          for (const subarea of deletedSubareas) {
            await subareaService.deleteSubarea(subarea.id);
          }

          // Process updates
          for (const subarea of updatedSubareas) {
            await subareaService.updateSubarea(subarea.id, {
              name: subarea.name,
              description: subarea.description,
              areaId: subarea.areaId
            });
          }

          // Process creations
          for (const subarea of newSubareas) {
            // Map the areaId to the real backend area ID
            const realAreaId = areaIdMapping.get(subarea.areaId) || subarea.areaId;
            
            await subareaService.createSubarea({
              name: subarea.name,
              description: subarea.description,
              areaId: realAreaId
            });
          }

          // Refresh from backend
          const freshSubareas = await subareaService.getSubareas();
          set({ subareas: freshSubareas, dirtySubareas: freshSubareas });
        } catch (error) {
          console.error("Failed to save subareas:", error);
          throw error;
        } finally {
          set({ isSaving: false });
        }
      },

      resetSubareas: () => {
        const { subareas } = get();
        set({ dirtySubareas: subareas });
      },

      getSubareasByAreaId: (areaId) => get().dirtySubareas.filter((s) => s.areaId === areaId),

      getDefaultAreaId: () => {
        const dirtyAreas = get().dirtyAreas;
        const defaultArea = dirtyAreas.find((a) => a.isDefault);
        return defaultArea ? defaultArea.id : null;
      },

      saveStep: async (stepId: number) => {
        if (stepId === 1) {
          await get().saveAreas();
        } else if (stepId === 2) {
          await get().saveSubareas();
        }
      },

      hasUnsavedChanges: () => {
        const { areas, dirtyAreas, subareas, dirtySubareas } = get();
        
        // Check if areas have changed
        if (areas.length !== dirtyAreas.length) return true;
        const areasChanged = areas.some(area => 
          !dirtyAreas.some(dirtyArea => 
            dirtyArea.id === area.id && 
            dirtyArea.name === area.name && 
            dirtyArea.description === area.description
          )
        ) || dirtyAreas.some(dirtyArea => 
          !areas.some(area => 
            area.id === dirtyArea.id && 
            area.name === dirtyArea.name && 
            area.description === dirtyArea.description
          )
        );
        
        // Check if subareas have changed
        if (subareas.length !== dirtySubareas.length) return true;
        const subareasChanged = subareas.some(subarea => 
          !dirtySubareas.some(dirtySubarea => 
            dirtySubarea.id === subarea.id && 
            dirtySubarea.name === subarea.name && 
            dirtySubarea.description === subarea.description &&
            dirtySubarea.areaId === subarea.areaId
          )
        ) || dirtySubareas.some(dirtySubarea => 
          !subareas.some(subarea => 
            subarea.id === dirtySubarea.id && 
            subarea.name === dirtySubarea.name && 
            subarea.description === dirtySubarea.description &&
            subarea.areaId === dirtySubarea.areaId
          )
        );
        
        return areasChanged || subareasChanged;
      },
    }),
    {
      name: 'wizard-storage',
      partialize: (state) => ({
        areas: state.areas,
        subareas: state.subareas,
        dirtyAreas: state.dirtyAreas,
        dirtySubareas: state.dirtySubareas,
      }),
    }
  )
); 