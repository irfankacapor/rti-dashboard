import React, { useEffect, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, CircularProgress, Alert } from '@mui/material';
import { indicatorValuesService } from '@/services/indicatorValuesService';
import { IndicatorValuesResponse, IndicatorValueEdit, IndicatorValueRow } from '@/types/indicatorValues';
import { useWizardStore } from '@/store/wizardStore';
import EditableValuesTable from './EditableValuesTable';
import { validateValueChange } from '@/utils/valueValidation';

interface IndicatorValuesEditModalProps {
  open: boolean;
  onClose: () => void;
  indicatorId: string;
  indicatorName: string;
}

const IndicatorValuesEditModal: React.FC<IndicatorValuesEditModalProps> = ({
  open, onClose, indicatorId, indicatorName
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<IndicatorValuesResponse | null>(null);
  const [localEdits, setLocalEdits] = useState<Record<string, IndicatorValueEdit>>({});
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const indicatorValueEditsMap = useWizardStore(s => s.indicatorValueEdits);
  const indicatorValueEdits = indicatorValueEditsMap[indicatorId] || [];
  const setIndicatorValueEdits = useWizardStore(s => s.setIndicatorValueEdits);
  const clearIndicatorValueEdits = useWizardStore(s => s.clearIndicatorValueEdits);

  useEffect(() => {
    if (open && indicatorId) {
      setLoading(true);
      setError(null);
      indicatorValuesService.getIndicatorValues(indicatorId)
        .then(res => {
          setData(res);
          // Preload local edits from store
          const editMap: Record<string, IndicatorValueEdit> = {};
          indicatorValueEdits.forEach((edit: IndicatorValueEdit) => {
            editMap[edit.factId] = edit;
          });
          setLocalEdits(editMap);
        })
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
    }
  }, [open, indicatorId]);

  const handleValueChange = (factId: string, newValue: string) => {
    if (!data) return;
    const row = data.rows.find(r => r.factId === factId);
    if (!row) return;
    const validation = validateValueChange(row.value, newValue, data.dataType);
    setValidationErrors(prev => ({ ...prev, [factId]: validation.error || '' }));
    setLocalEdits(prev => ({
      ...prev,
      [factId]: {
        factId,
        originalValue: row.value,
        newValue: parseFloat(newValue),
        isNew: row.isEmpty,
      }
    }));
  };

  const handleSave = async () => {
    // Validate all edits
    if (!data) return;
    let hasError = false;
    const newValidationErrors: Record<string, string> = {};
    Object.entries(localEdits).forEach(([factId, edit]) => {
      const row = data.rows.find(r => r.factId === factId);
      if (!row) return;
      const validation = validateValueChange(row.value, String(edit.newValue), data.dataType);
      if (!validation.isValid) {
        hasError = true;
        newValidationErrors[factId] = validation.error || 'Invalid value';
      }
    });
    setValidationErrors(newValidationErrors);
    if (hasError) return;
    // Save edits to wizard store
    setIndicatorValueEdits(indicatorId, Object.values(localEdits));
    // Commit to backend
    try {
      await indicatorValuesService.updateIndicatorValues(
        indicatorId,
        Object.values(localEdits).map(edit => ({ factId: edit.factId, newValue: edit.newValue }))
      );
      onClose();
    } catch (e: any) {
      setError(e.message || 'Failed to save changes');
    }
  };

  const handleCancel = () => {
    // Discard local edits
    setLocalEdits({});
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleCancel} maxWidth="lg" fullWidth>
      <DialogTitle>Edit Values for {indicatorName}</DialogTitle>
      <DialogContent>
        {loading && <CircularProgress />}
        {error && <Alert severity="error">{error}</Alert>}
        {data && (
          <EditableValuesTable
            data={data.rows}
            dimensions={data.dimensionColumns}
            indicatorName={data.indicatorName}
            onValueChange={handleValueChange}
            editedValues={localEdits}
            validationErrors={validationErrors}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancel}>Cancel</Button>
        <Button onClick={handleSave} variant="contained" color="primary">Save</Button>
      </DialogActions>
    </Dialog>
  );
};

export default IndicatorValuesEditModal; 