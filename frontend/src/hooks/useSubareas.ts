import { useCallback, useState } from 'react';
import { useWizardStore } from '@/store/wizardStore';
import * as subareaService from '@/services/subareaService';
import { SubareaFormData } from '@/types/subareas';

export function useSubareas() {
  const setSubareas = useWizardStore((state) => (subs: any) => state.subareas = subs);
  const addSubarea = useWizardStore((state) => state.addSubarea);
  const updateSubarea = useWizardStore((state) => state.updateSubarea);
  const deleteSubarea = useWizardStore((state) => state.deleteSubarea);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSubareas = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const subs = await subareaService.getSubareas();
      setSubareas(subs);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [setSubareas]);

  const create = useCallback(async (form: SubareaFormData) => {
    setLoading(true);
    setError(null);
    try {
      const sub = await subareaService.createSubarea(form);
      addSubarea(sub);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [addSubarea]);

  const update = useCallback(async (id: string, updates: Partial<SubareaFormData>) => {
    setLoading(true);
    setError(null);
    try {
      const sub = await subareaService.updateSubarea(id, updates);
      updateSubarea(id, sub);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [updateSubarea]);

  const remove = useCallback(async (id: string) => {
    setLoading(true);
    setError(null);
    try {
      await subareaService.deleteSubarea(id);
      deleteSubarea(id);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [deleteSubarea]);

  return { loading, error, fetchSubareas, create, update, remove };
} 