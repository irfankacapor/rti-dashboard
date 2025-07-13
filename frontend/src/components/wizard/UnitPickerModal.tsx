import React, { useEffect, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Autocomplete, CircularProgress, Box, Typography } from '@mui/material';
import { unitService } from '@/services/unitService';
import { UnitResponse } from '@/types/indicators';

interface UnitPickerModalProps {
  open: boolean;
  initialUnit?: string;
  initialPrefix?: string;
  initialSuffix?: string;
  onSave: (unit: string, prefix: string, suffix: string) => void;
  onClose: () => void;
}

export const UnitPickerModal: React.FC<UnitPickerModalProps> = ({ open, initialUnit, initialPrefix, initialSuffix, onSave, onClose }) => {
  const [groupedUnits, setGroupedUnits] = useState<Record<string, UnitResponse[]>>({});
  const [loading, setLoading] = useState(false);
  const [unit, setUnit] = useState<string>(initialUnit || '');
  const [prefix, setPrefix] = useState<string>(initialPrefix || '');
  const [suffix, setSuffix] = useState<string>(initialSuffix || '');
  const [unitOptions, setUnitOptions] = useState<UnitResponse[]>([]);

  useEffect(() => {
    if (open) {
      setLoading(true);
      unitService.getGroupedUnits()
        .then((data) => {
          setGroupedUnits(data);
          setUnitOptions(Object.values(data).flat());
        })
        .finally(() => setLoading(false));
    }
  }, [open]);

  useEffect(() => {
    setUnit(initialUnit || '');
    setPrefix(initialPrefix || '');
    setSuffix(initialSuffix || '');
  }, [initialUnit, initialPrefix, initialSuffix, open]);

  const handleSave = () => {
    if (unit) {
      onSave(unit, prefix, suffix);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Pick Unit</DialogTitle>
      <DialogContent>
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={120}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Box display="flex" gap={2} alignItems="center" mb={2}>
              <TextField
                label="Prefix"
                value={prefix}
                onChange={e => setPrefix(e.target.value)}
                size="small"
                sx={{ flex: 1 }}
              />
              <Autocomplete
                options={unitOptions}
                groupBy={option => option.group ? option.group : 'Other'}
                getOptionLabel={option => `${option.code} - ${option.description || ''}`}
                value={unitOptions.find(u => u.code === unit) || null}
                onChange={(_, value) => setUnit(value ? value.code : '')}
                renderInput={(params) => <TextField {...params} label="Unit" size="small" required />} 
                sx={{ flex: 2 }}
                isOptionEqualToValue={(option, value) => option.code === value.code}
              />
              <TextField
                label="Suffix"
                value={suffix}
                onChange={e => setSuffix(e.target.value)}
                size="small"
                sx={{ flex: 1 }}
              />
            </Box>
            <Typography variant="caption" color="text.secondary">
              Example: {prefix ? prefix + ' ' : ''}{unit}{suffix ? ' ' + suffix : ''}
            </Typography>
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSave} variant="contained" disabled={!unit}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}; 