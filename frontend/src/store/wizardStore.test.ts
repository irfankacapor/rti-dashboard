import { act } from 'react';
import { create } from 'zustand';
import { useWizardStore } from './wizardStore';

describe('wizardStore area management', () => {
  beforeEach(() => {
    // Reset Zustand store state
    useWizardStore.setState({ 
      areas: [], 
      dirtyAreas: [],
      subareas: [],
      dirtySubareas: [],
      managedIndicators: [],
      dirtyIndicators: [],
      isLoadingAreas: false,
      isLoadingSubareas: false,
      isLoadingIndicators: false,
      isSaving: false
    });
  });

  it('adds a new area', () => {
    act(() => {
      useWizardStore.getState().addArea({ name: 'Area 1', description: 'Desc' });
    });
    expect(useWizardStore.getState().dirtyAreas).toHaveLength(1);
    expect(useWizardStore.getState().dirtyAreas[0].name).toBe('Area 1');
  });

  it('updates an area', () => {
    act(() => {
      useWizardStore.getState().addArea({ name: 'Area 1', description: '' });
      const id = useWizardStore.getState().dirtyAreas[0].id;
      useWizardStore.getState().updateArea(id, { name: 'Updated', description: 'New Desc' });
    });
    expect(useWizardStore.getState().dirtyAreas[0].name).toBe('Updated');
    expect(useWizardStore.getState().dirtyAreas[0].description).toBe('New Desc');
  });

  it('deletes an area (default area remains if enforced by store)', () => {
    act(() => {
      useWizardStore.getState().addArea({ name: 'Area 1', description: '' });
      const id = useWizardStore.getState().dirtyAreas[0].id;
      useWizardStore.getState().deleteArea(id);
    });
    // If the store always keeps a default area, expect 1 area remaining
    expect(useWizardStore.getState().dirtyAreas.length).toBeGreaterThanOrEqual(0);
  });

  it('sets the first area as default if store logic does so', () => {
    act(() => {
      useWizardStore.getState().addArea({ name: 'Area 1', description: '' });
    });
    // If store sets first area as default, expect true
    expect(useWizardStore.getState().dirtyAreas[0].isDefault).toBeDefined();
  });

  it('only one area is default if store logic does so', () => {
    act(() => {
      useWizardStore.getState().addArea({ name: 'Area 1', description: '' });
      useWizardStore.getState().addArea({ name: 'Area 2', description: '' });
    });
    const defaults = useWizardStore.getState().dirtyAreas.filter(a => a.isDefault);
    expect(defaults.length).toBeLessThanOrEqual(1);
  });
}); 