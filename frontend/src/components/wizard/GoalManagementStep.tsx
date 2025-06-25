import React, { useEffect, useState } from 'react';
import { Box, Typography, Button, Paper, CircularProgress, Alert, Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, MenuItem, Select, Checkbox, ListItemText, FormControl, InputLabel, OutlinedInput, List, ListItem, ListItemSecondaryAction } from '@mui/material';
import { useWizardStore } from '@/lib/store/useWizardStore';
import { goalService } from '@/services/goalService';
import { indicatorManagementService } from '@/services/indicatorManagementService';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

interface Goal {
  id: number;
  name: string;
  description?: string;
  type?: string;
  indicators?: number[];
  targets?: GoalTarget[];
}

interface GoalTarget {
  id?: number;
  value: number;
  deadline?: string;
}

interface Indicator {
  id: number;
  name: string;
}

export const GoalManagementStep: React.FC = () => {
  const { setStepCompleted, setStepValid, nextStep } = useWizardStore();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingGoal, setEditingGoal] = useState<Goal | null>(null);
  const [indicators, setIndicators] = useState<Indicator[]>([]);

  useEffect(() => {
    setStepValid(5, true); // Always valid (optional step)
    setStepCompleted(5, true); // Mark as completed if skipped
  }, [setStepValid, setStepCompleted]);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      goalService.getGoals(),
      indicatorManagementService.getIndicators()
    ])
      .then(([goals, indicators]) => {
        setGoals(goals);
        setIndicators(indicators.map((i: any) => ({ id: i.id, name: i.name })));
      })
      .catch((e: any) => setError(e.message || 'Failed to fetch data'))
      .finally(() => setLoading(false));
  }, []);

  const handleSkip = () => {
    setStepValid(5, true);
    setStepCompleted(5, true);
    nextStep();
  };

  const handleAddGoal = () => {
    setEditingGoal({ id: 0, name: '', description: '', type: '', indicators: [], targets: [] });
    setOpenDialog(true);
  };

  const handleEditGoal = (goal: Goal) => {
    setEditingGoal(goal);
    setOpenDialog(true);
  };

  const handleDeleteGoal = async (goalId: number) => {
    setLoading(true);
    setError(null);
    try {
      await goalService.deleteGoal(goalId);
      const updatedGoals = await goalService.getGoals();
      setGoals(updatedGoals);
    } catch (e: any) {
      setError(e.message || 'Failed to delete goal');
    } finally {
      setLoading(false);
    }
  };

  const handleDialogClose = () => {
    setOpenDialog(false);
    setEditingGoal(null);
  };

  const handleDialogSave = async () => {
    setLoading(true);
    setError(null);
    try {
      if (editingGoal) {
        if (editingGoal.id === 0) {
          await goalService.createGoal(editingGoal);
        } else {
          await goalService.updateGoal(editingGoal.id, editingGoal);
        }
        const updatedGoals = await goalService.getGoals();
        setGoals(updatedGoals);
      }
      setOpenDialog(false);
      setEditingGoal(null);
    } catch (e: any) {
      setError(e.message || 'Failed to save goal');
    } finally {
      setLoading(false);
    }
  };

  // Indicator selection
  const handleIndicatorChange = (event: any) => {
    if (editingGoal) {
      setEditingGoal({ ...editingGoal, indicators: event.target.value });
    }
  };

  // Target management
  const handleAddTarget = () => {
    if (editingGoal) {
      setEditingGoal({ ...editingGoal, targets: [...(editingGoal.targets || []), { value: 0, deadline: '' }] });
    }
  };
  const handleTargetChange = (idx: number, field: string, value: any) => {
    if (editingGoal) {
      const newTargets = [...(editingGoal.targets || [])];
      newTargets[idx] = { ...newTargets[idx], [field]: value };
      setEditingGoal({ ...editingGoal, targets: newTargets });
    }
  };
  const handleDeleteTarget = (idx: number) => {
    if (editingGoal) {
      const newTargets = [...(editingGoal.targets || [])];
      newTargets.splice(idx, 1);
      setEditingGoal({ ...editingGoal, targets: newTargets });
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Goals & Targets</Typography>
        <Button variant="outlined" color="secondary" onClick={handleSkip}>
          Skip
        </Button>
      </Box>
      <Button variant="contained" color="primary" onClick={handleAddGoal} sx={{ mb: 2 }}>
        Add Goal
      </Button>
      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight={120}>
          <CircularProgress />
        </Box>
      ) : error ? (
        <Alert severity="error">{error}</Alert>
      ) : goals.length === 0 ? (
        <Alert severity="info">No goals defined yet.</Alert>
      ) : (
        <Paper sx={{ p: 2 }}>
          {goals.map(goal => (
            <Box key={goal.id} mb={2} display="flex" alignItems="center" justifyContent="space-between">
              <Box>
                <Typography variant="subtitle1">{goal.name}</Typography>
                <Typography variant="body2" color="text.secondary">{goal.description}</Typography>
              </Box>
              <Box>
                <IconButton onClick={() => handleEditGoal(goal)}><EditIcon /></IconButton>
                <IconButton onClick={() => handleDeleteGoal(goal.id)}><DeleteIcon /></IconButton>
              </Box>
            </Box>
          ))}
        </Paper>
      )}
      {/* Add/Edit Goal Dialog */}
      <Dialog open={openDialog} onClose={handleDialogClose} maxWidth="md" fullWidth>
        <DialogTitle>{editingGoal?.id === 0 ? 'Add Goal' : 'Edit Goal'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Goal Name"
            value={editingGoal?.name || ''}
            onChange={e => setEditingGoal(editingGoal ? { ...editingGoal, name: e.target.value } : null)}
            fullWidth
            required
            sx={{ mb: 2 }}
          />
          <TextField
            label="Description"
            value={editingGoal?.description || ''}
            onChange={e => setEditingGoal(editingGoal ? { ...editingGoal, description: e.target.value } : null)}
            fullWidth
            multiline
            rows={2}
            sx={{ mb: 2 }}
          />
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Type</InputLabel>
            <Select
              value={editingGoal?.type || ''}
              onChange={e => setEditingGoal(editingGoal ? { ...editingGoal, type: e.target.value } : null)}
              input={<OutlinedInput label="Type" />}
            >
              <MenuItem value="">None</MenuItem>
              <MenuItem value="quantitative">Quantitative</MenuItem>
              <MenuItem value="qualitative">Qualitative</MenuItem>
            </Select>
          </FormControl>
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Indicators</InputLabel>
            <Select
              multiple
              value={editingGoal?.indicators || []}
              onChange={handleIndicatorChange}
              input={<OutlinedInput label="Indicators" />}
              renderValue={(selected) => indicators.filter(i => (selected as number[]).includes(i.id)).map(i => i.name).join(', ')}
            >
              {indicators.map(ind => (
                <MenuItem key={ind.id} value={ind.id}>
                  <Checkbox checked={!!editingGoal?.indicators?.includes(ind.id)} />
                  <ListItemText primary={ind.name} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Targets</Typography>
            <List>
              {(editingGoal?.targets || []).map((target, idx) => (
                <ListItem key={idx}>
                  <TextField
                    label="Value"
                    type="number"
                    value={target.value}
                    onChange={e => handleTargetChange(idx, 'value', e.target.value)}
                    sx={{ mr: 2 }}
                  />
                  <TextField
                    label="Deadline"
                    type="date"
                    value={target.deadline || ''}
                    onChange={e => handleTargetChange(idx, 'deadline', e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    sx={{ mr: 2 }}
                  />
                  <ListItemSecondaryAction>
                    <IconButton edge="end" onClick={() => handleDeleteTarget(idx)}><DeleteIcon /></IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
            <Button onClick={handleAddTarget} variant="outlined" size="small">Add Target</Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose}>Cancel</Button>
          <Button onClick={handleDialogSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}; 