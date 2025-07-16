import React, { useEffect, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, CircularProgress, Alert } from '@mui/material';
import { indicatorValuesService } from '@/services/indicatorValuesService';
import { IndicatorValuesResponse, IndicatorValueEdit, NewIndicatorValueRow, IndicatorValueCreate } from '@/types/indicatorValues';
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
  const [success, setSuccess] = useState<string | null>(null);
  const [data, setData] = useState<IndicatorValuesResponse | null>(null);
  const [localEdits, setLocalEdits] = useState<Record<string, IndicatorValueEdit>>({});
  const [newRows, setNewRows] = useState<NewIndicatorValueRow[]>([]);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const indicatorValueEditsMap = useWizardStore(s => s.indicatorValueEdits);
  const indicatorValueEdits = indicatorValueEditsMap[indicatorId] || [];
  const setIndicatorValueEdits = useWizardStore(s => s.setIndicatorValueEdits);
  const clearIndicatorValueEdits = useWizardStore(s => s.clearIndicatorValueEdits);
  const fetchManagedIndicators = useWizardStore(s => s.fetchManagedIndicators);

  useEffect(() => {
    if (open && indicatorId) {
      setLoading(true);
      setError(null);
      setSuccess(null);
      indicatorValuesService.getIndicatorValues(indicatorId)
        .then(res => {
          setData(res);
          // Preload local edits from store
          const editMap: Record<string, IndicatorValueEdit> = {};
          indicatorValueEdits.forEach((edit: IndicatorValueEdit) => {
            editMap[edit.factId] = edit;
          });
          setLocalEdits(editMap);
          setNewRows([]); // Reset new rows when opening modal
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

  const handleNewRowChange = (tempId: string, field: string, value: string) => {
    setNewRows(prev => prev.map(row => {
      if (row.tempId === tempId) {
        if (field === 'value') {
          const validation = validateValueChange(undefined, value, data?.dataType || 'decimal');
          setValidationErrors(prevErrors => ({ 
            ...prevErrors, 
            [tempId]: validation.error || '' 
          }));
          return { ...row, value: parseFloat(value) || 0 };
        } else {
          return { 
            ...row, 
            dimensions: { ...row.dimensions, [field]: value } 
          };
        }
      }
      return row;
    }));
  };

  const handleAddNewRow = () => {
    if (!data) return;
    const tempId = `new_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const newRow: NewIndicatorValueRow = {
      tempId,
      dimensions: {},
      value: 0,
      isNewRow: true,
    };
    setNewRows(prev => [...prev, newRow]);
  };

  const handleRemoveNewRow = (tempId: string) => {
    setNewRows(prev => prev.filter(row => row.tempId !== tempId));
    setValidationErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors[tempId];
      return newErrors;
    });
  };

  const validateNewRows = (): boolean => {
    if (!data) return true;
    
    let isValid = true;
    const newErrors: Record<string, string> = { ...validationErrors };
    
    newRows.forEach(row => {
      // Check if all dimensions are filled
      const missingDimensions = data.dimensionColumns.filter(dim => !row.dimensions[dim] || row.dimensions[dim].trim() === '');
      if (missingDimensions.length > 0) {
        newErrors[row.tempId] = `Missing dimensions: ${missingDimensions.join(', ')}`;
        isValid = false;
      } else {
        delete newErrors[row.tempId];
      }
      
      // Check if value is valid
      const valueValidation = validateValueChange(undefined, String(row.value), data.dataType);
      if (!valueValidation.isValid) {
        newErrors[row.tempId] = valueValidation.error || 'Invalid value';
        isValid = false;
      }
    });
    
    setValidationErrors(newErrors);
    return isValid;
  };

  const handleSave = async () => {
    if (!data) return;
    
    // Validate all edits
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
    
    // Validate new rows
    const newRowsValid = validateNewRows();
    if (!newRowsValid) {
      hasError = true;
    }
    
    setValidationErrors({ ...newValidationErrors, ...validationErrors });
    if (hasError) return;
    
    try {
      // Save edits to backend
      if (Object.keys(localEdits).length > 0) {
        await indicatorValuesService.updateIndicatorValues(
          indicatorId,
          Object.values(localEdits).map(edit => ({ factId: edit.factId, newValue: edit.newValue }))
        );
      }
      
      // Save new rows to backend
      if (newRows.length > 0) {
        const newValues: IndicatorValueCreate[] = newRows.map(row => ({
          dimensions: row.dimensions,
          value: row.value,
        }));
        await indicatorValuesService.createIndicatorValues(indicatorId, newValues);
      }
      
      // Save edits to wizard store
      setIndicatorValueEdits(indicatorId, Object.values(localEdits));
      
      // Refresh indicator data to update valueCount
      await fetchManagedIndicators();
      
      // Show success message
      const totalChanges = Object.keys(localEdits).length + newRows.length;
      setSuccess(`Successfully saved ${totalChanges} value(s). The indicator count has been updated.`);
      
      // Close modal after a short delay to show success message
      setTimeout(() => {
        onClose();
      }, 1500);
      
    } catch (e: any) {
      setError(e.message || 'Failed to save changes');
    }
  };

  const handleCancel = () => {
    // Discard local edits and new rows
    setLocalEdits({});
    setNewRows([]);
    setValidationErrors({});
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleCancel} maxWidth="lg" fullWidth>
      <DialogTitle>Edit Values for {indicatorName}</DialogTitle>
      <DialogContent>
        {loading && <CircularProgress />}
        {error && <Alert severity="error">{error}</Alert>}
        {success && <Alert severity="success">{success}</Alert>}
        {data && (
          <EditableValuesTable
            data={data.rows}
            dimensions={data.dimensionColumns}
            indicatorName={data.indicatorName}
            onValueChange={handleValueChange}
            onNewRowChange={handleNewRowChange}
            onAddNewRow={handleAddNewRow}
            onRemoveNewRow={handleRemoveNewRow}
            editedValues={localEdits}
            newRows={newRows}
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