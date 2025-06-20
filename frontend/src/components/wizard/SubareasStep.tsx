import React, { useMemo, useEffect, useRef, useState } from 'react';
import { Box, Typography, Button, Alert, CircularProgress, Snackbar } from '@mui/material';
import { useWizardStore } from '@/store/wizardStore';
import { useWizardStore as useMainWizardStore } from '@/lib/store/useWizardStore';
import { SubareasTable } from '../common/SubareasTable';
import { SubareaFormData } from '@/types/subareas';

export const SubareasStep: React.FC = () => {
  const areas = useWizardStore((state) => state.areas);
  const subareas = useWizardStore((state) => state.subareas);
  const addSubarea = useWizardStore((state) => state.addSubarea);
  const updateSubarea = useWizardStore((state) => state.updateSubarea);
  const deleteSubarea = useWizardStore((state) => state.deleteSubarea);
  const getDefaultAreaId = useWizardStore((state) => state.getDefaultAreaId);
  const fetchSubareas = useWizardStore((state) => state.fetchSubareas);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);

  // Main wizard store for step completion
  const setStepCompleted = useMainWizardStore((state) => state.setStepCompleted);
  const setStepValid = useMainWizardStore((state) => state.setStepValid);

  // Always treat areas as an array for safety in tests and runtime
  const safeAreas = Array.isArray(areas) ? areas : [];
  // Only show non-default areas in picker
  const manualAreas = useMemo(() => safeAreas.filter(a => !a.isDefault), [safeAreas]);
  const showAreaColumn = manualAreas.length > 0;

  // Validation: at least 1 subarea required
  const isStepValid = subareas.length > 0;

  // Mark step as completed and valid when validation passes
  useEffect(() => {
    setStepValid(2, isStepValid);
    setStepCompleted(2, isStepValid);
  }, [isStepValid, setStepCompleted, setStepValid]);

  // Effect: If manual areas are added after subareas exist, reassign subareas from default area to first manual area
  const prevManualAreasCount = useRef(manualAreas.length);
  useEffect(() => {
    if (prevManualAreasCount.current === 0 && manualAreas.length > 0) {
      const defaultAreaId = getDefaultAreaId();
      const firstManualAreaId = manualAreas[0]?.id;
      if (firstManualAreaId) {
        subareas.forEach((sub) => {
          if (sub.areaId === defaultAreaId) {
            updateSubarea(sub.id, { areaId: firstManualAreaId });
          }
        });
      }
    }
    prevManualAreasCount.current = manualAreas.length;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [manualAreas.length]);

  // Fetch subareas on mount
  useEffect(() => {
    setIsLoading(true);
    fetchSubareas().catch((err) => setError((err as Error).message)).finally(() => setIsLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Handler for adding subarea
  const handleAdd = async (formData: SubareaFormData) => {
    let areaId = formData.areaId;
    if (!showAreaColumn) {
      areaId = getDefaultAreaId() || '';
    }
    setIsLoading(true);
    setError(null);
    try {
      await addSubarea({ ...formData, areaId });
      setSnackbar('Subarea added successfully');
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdate = async (id: string, updates: Partial<SubareaFormData>) => {
    setIsLoading(true);
    setError(null);
    try {
      await updateSubarea(id, updates);
      setSnackbar('Subarea updated successfully');
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await deleteSubarea(id);
      setSnackbar('Subarea deleted');
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Manage Subareas
      </Typography>
      <Typography variant="body2" sx={{ mb: 2 }}>
        Create subareas and assign them to areas. Each subarea must belong to exactly one area.
      </Typography>
      {!isStepValid && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          You must add at least one subarea to continue.
        </Alert>
      )}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      )}
      {isLoading && <Box mt={2} mb={2}><CircularProgress /></Box>}
      <SubareasTable
        subareas={subareas}
        areas={manualAreas}
        allAreas={safeAreas}
        onAdd={handleAdd}
        onUpdate={handleUpdate}
        onDelete={handleDelete}
        showAreaColumn={showAreaColumn}
        isWizardMode={true}
        allowEdit={true}
      />
      <Snackbar
        open={!!snackbar}
        autoHideDuration={3000}
        onClose={() => setSnackbar(null)}
        message={snackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
      {/* Wizard navigation and validation handled by parent WizardLayout */}
    </Box>
  );
}; 