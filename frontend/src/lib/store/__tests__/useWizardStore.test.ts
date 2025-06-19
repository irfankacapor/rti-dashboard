import { renderHook, act } from '@testing-library/react';
import { useWizardStore } from '../useWizardStore';

describe('useWizardStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useWizardStore.getState().resetWizard();
  });

  it('initializes with correct default state', () => {
    const { result } = renderHook(() => useWizardStore());
    
    expect(result.current.currentStep).toBe(1);
    expect(result.current.steps).toHaveLength(5);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.steps[0]).toEqual({
      id: 1,
      name: 'areas',
      label: 'Areas Management',
      isCompleted: false,
      isValid: false,
    });
  });

  it('allows proceeding to step 1 initially', () => {
    const { result } = renderHook(() => useWizardStore());
    
    expect(result.current.canProceedToStep(1)).toBe(true);
    expect(result.current.canProceedToStep(2)).toBe(false);
  });

  it('sets current step correctly when allowed', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setCurrentStep(1);
    });
    
    expect(result.current.currentStep).toBe(1);
  });

  it('does not allow setting step when not allowed', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setCurrentStep(3);
    });
    
    expect(result.current.currentStep).toBe(1); // Should remain 1
  });

  it('marks step as completed', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setStepCompleted(1, true);
    });
    
    const step1 = result.current.steps.find(s => s.id === 1);
    expect(step1?.isCompleted).toBe(true);
  });

  it('marks step as valid', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setStepValid(1, true);
    });
    
    const step1 = result.current.steps.find(s => s.id === 1);
    expect(step1?.isValid).toBe(true);
  });

  it('allows proceeding to next step after completing previous', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setStepCompleted(1, true);
    });
    
    expect(result.current.canProceedToStep(2)).toBe(true);
  });

  it('moves to next step correctly', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setStepCompleted(1, true);
      result.current.nextStep();
    });
    
    expect(result.current.currentStep).toBe(2);
  });

  it('moves to previous step correctly', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setStepCompleted(1, true);
      result.current.setCurrentStep(2);
      result.current.prevStep();
    });
    
    expect(result.current.currentStep).toBe(1);
  });

  it('does not go below step 1', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.prevStep();
    });
    
    expect(result.current.currentStep).toBe(1);
  });

  it('sets loading state correctly', () => {
    const { result } = renderHook(() => useWizardStore());
    
    act(() => {
      result.current.setLoading(true);
    });
    
    expect(result.current.isLoading).toBe(true);
  });

  it('resets wizard state correctly', () => {
    const { result } = renderHook(() => useWizardStore());
    
    // Modify state first
    act(() => {
      result.current.setCurrentStep(1);
      result.current.setStepCompleted(1, true);
      result.current.setLoading(true);
    });
    
    // Reset
    act(() => {
      result.current.resetWizard();
    });
    
    expect(result.current.currentStep).toBe(1);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.steps[0].isCompleted).toBe(false);
  });
}); 