import React, { useMemo, useEffect, useRef } from 'react';
import { Box, Typography, Button, Alert } from '@mui/material';
import { useWizardStore } from '@/store/wizardStore';
import { SubareasTable } from '../common/SubareasTable';
import { SubareaFormData } from '@/types/subareas';

export const SubareasStep: React.FC = () => {
  const areas = useWizardStore((state) => state.areas);
  const subareas = useWizardStore((state) => state.subareas);
  const addSubarea = useWizardStore((state) => state.addSubarea);
  const updateSubarea = useWizardStore((state) => state.updateSubarea);
  const deleteSubarea = useWizardStore((state) => state.deleteSubarea);
  const getDefaultAreaId = useWizardStore((state) => state.getDefaultAreaId);

  // Always treat areas as an array for safety in tests and runtime
  const safeAreas = Array.isArray(areas) ? areas : [];
  // Only show non-default areas in picker
  const manualAreas = useMemo(() => safeAreas.filter(a => !a.isDefault), [safeAreas]);
  const showAreaColumn = manualAreas.length > 0;

  // Validation: at least 1 subarea required
  const isStepValid = subareas.length > 0;

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

  // Handler for adding subarea
  const handleAdd = (formData: SubareaFormData) => {
    let areaId = formData.areaId;
    // If no manual areas, always assign to default area
    if (!showAreaColumn) {
      areaId = getDefaultAreaId() || '';
    }
    addSubarea({ ...formData, areaId, code: '' });
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
      <SubareasTable
        subareas={subareas}
        areas={manualAreas}
        allAreas={safeAreas}
        onAdd={handleAdd}
        onUpdate={updateSubarea}
        onDelete={deleteSubarea}
        showAreaColumn={showAreaColumn}
        isWizardMode={true}
        allowEdit={true}
      />
      {/* Wizard navigation and validation handled by parent WizardLayout */}
    </Box>
  );
}; 