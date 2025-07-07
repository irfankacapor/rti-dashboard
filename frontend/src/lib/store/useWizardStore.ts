import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface WizardStep {
  id: number;
  name: string;
  label: string;
  isCompleted: boolean;
  isValid: boolean;
}

export interface WizardState {
  currentStep: number;
  steps: WizardStep[];
  isLoading: boolean;
  managedIndicators: any[];
  
  // Actions
  setCurrentStep: (step: number) => void;
  nextStep: () => void;
  prevStep: () => void;
  setStepCompleted: (stepId: number, completed: boolean) => void;
  setStepValid: (stepId: number, valid: boolean) => void;
  setLoading: (loading: boolean) => void;
  canProceedToStep: (stepId: number) => boolean;
  resetWizard: () => void;
}

const initialSteps: WizardStep[] = [
  { id: 1, name: 'areas', label: 'Areas Management', isCompleted: false, isValid: false },
  { id: 2, name: 'subareas', label: 'Subareas Management', isCompleted: false, isValid: false },
  { id: 3, name: 'upload', label: 'Data Upload', isCompleted: false, isValid: false },
  { id: 4, name: 'indicators', label: 'Indicators Processing', isCompleted: false, isValid: false },
  { id: 5, name: 'goals', label: 'Goals & Targets', isCompleted: false, isValid: false },
];

export const useWizardStore = create<WizardState>()(
  persist(
    (set, get) => ({
      currentStep: 1,
      steps: initialSteps,
      isLoading: false,
      managedIndicators: [],

      setCurrentStep: (step: number) => {
        if (get().canProceedToStep(step)) {
          set({ currentStep: step });
        }
      },

      nextStep: () => {
        const { currentStep, steps } = get();
        
        if (currentStep < steps.length) {
          const nextStepId = currentStep + 1;
          
          if (get().canProceedToStep(nextStepId)) {
            set({ currentStep: nextStepId });
          }
        }
      },

      prevStep: () => {
        const { currentStep } = get();
        if (currentStep > 1) {
          set({ currentStep: currentStep - 1 });
        }
      },

      setStepCompleted: (stepId: number, completed: boolean) =>
        set((state) => ({
          steps: state.steps.map((step) =>
            step.id === stepId ? { ...step, isCompleted: completed } : step
          ),
        })),

      setStepValid: (stepId: number, valid: boolean) =>
        set((state) => ({
          steps: state.steps.map((step) =>
            step.id === stepId ? { ...step, isValid: valid } : step
          ),
        })),

      setLoading: (loading: boolean) => set({ isLoading: loading }),

      canProceedToStep: (stepId: number) => {
        const { steps, managedIndicators } = get();
        
        if (stepId === 1) {
          return true;
        }
        
        // Allow direct access to step 4 if indicators exist
        if (stepId === 4 && managedIndicators && managedIndicators.length > 0) {
          return true;
        }
        
        // Can proceed if all previous steps are completed
        for (let i = 1; i < stepId; i++) {
          const step = steps.find(s => s.id === i);
          if (!step?.isCompleted) {
            return false;
          }
        }
        
        return true;
      },

      resetWizard: () =>
        set({
          currentStep: 1,
          steps: initialSteps,
          isLoading: false,
          managedIndicators: [],
        }),
    }),
    {
      name: 'wizard-storage',
    }
  )
); 