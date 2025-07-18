import React, { useEffect, useState } from 'react';
import { Box, Typography, Button, Paper, CircularProgress, Alert, Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, MenuItem, Select, Checkbox, ListItemText, FormControl, InputLabel, OutlinedInput, List, ListItem, ListItemSecondaryAction } from '@mui/material';
import { useWizardStore } from '@/lib/store/useWizardStore';
import { goalService } from '@/services/goalService';
import { indicatorManagementService } from '@/services/indicatorManagementService';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import { GoalGroup } from '@/services/goalService';

interface Goal {
  id: number;
  name: string;
  description?: string;
  type?: string;
  indicators?: number[];
  targets?: GoalTarget[];
  group?: number;
  goalGroup?: GoalGroup;
  year: number;
}

interface GoalTarget {
  id?: number;
  value: number; // This will be used as targetValue
  targetYear: number;
  deadline?: string;
  unit?: string;
  targetType: string;
  indicatorId: number;
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
  const [groups, setGroups] = useState<GoalGroup[]>([]);
  const [addGroupModalOpen, setAddGroupModalOpen] = useState(false);
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDescription, setNewGroupDescription] = useState('');
  const [groupCreateStatus, setGroupCreateStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [groupCreateError, setGroupCreateError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setStepValid(5, true); // Always valid (optional step)
    setStepCompleted(5, true); // Mark as completed if skipped
  }, [setStepValid, setStepCompleted]);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      goalService.getGoals(),
      indicatorManagementService.getIndicators(),
      goalService.getGroups()
    ])
      .then(([goals, indicators, groups]) => {
        setGoals(goals.map(g => ({ 
          year: (g as any).year ?? new Date().getFullYear(), 
          ...g,
          group: g.goalGroup?.id // Map goalGroup to group for consistency
        })));
        setIndicators(indicators.map((i: any) => ({ id: i.id, name: i.name })));
        setGroups(groups);
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
    const defaultGroup = groups.length > 0 ? groups[0].id : undefined;
    setEditingGoal({ 
      id: 0, 
      name: '', 
      description: '', 
      type: '', 
      indicators: [], 
      targets: [], 
      group: defaultGroup, 
      year: new Date().getFullYear() 
    });
    setOpenDialog(true);
  };

  const handleEditGoal = (goal: Goal) => {
    // Map the goalGroup to the group field for the form
    const goalForEdit = {
      ...goal,
      group: goal.goalGroup?.id
    };
    setEditingGoal(goalForEdit);
    setOpenDialog(true);
  };

  const handleDeleteGoal = async (goalId: number) => {
    setLoading(true);
    setError(null);
    try {
      await goalService.deleteGoal(goalId);
      const updatedGoals = await goalService.getGoals();
      setGoals(updatedGoals.map(g => ({ 
        year: (g as any).year ?? new Date().getFullYear(), 
        ...g,
        group: g.goalGroup?.id // Map goalGroup to group for consistency
      })));
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
        // Use the selected group or fall back to the existing goal group
        const goalGroupId = editingGoal.group || editingGoal.goalGroup?.id;
        
        // Validate that a goal group is selected
        if (!goalGroupId) {
          setError('Please select a goal group');
          setLoading(false);
          return;
        }
        
        const payload = {
          ...editingGoal,
          goalGroupId: goalGroupId,
          year: editingGoal.year,
        };
        delete payload.group;
        delete payload.goalGroup;
        
        if (editingGoal.id === 0) {
          const createdGoal = await goalService.createGoal(payload);
          if (editingGoal.targets && editingGoal.targets.length > 0 && editingGoal.indicators && editingGoal.indicators.length > 0) {
            for (const target of editingGoal.targets) {
              for (const indicatorId of editingGoal.indicators) {
                await goalService.createGoalTarget({
                  ...target,
                  goalId: createdGoal.id,
                  indicatorId,
                  targetValue: target.value,
                });
              }
            }
          }
        } else {
          await goalService.updateGoal(editingGoal.id, payload);
          // Optionally update targets for existing goals here
        }
        const updatedGoals = await goalService.getGoals();
        setGoals(updatedGoals.map(g => ({ 
          year: (g as any).year ?? new Date().getFullYear(), 
          ...g,
          group: g.goalGroup?.id // Map goalGroup to group for consistency
        })));
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
      const defaultIndicatorId = indicators.length > 0 ? indicators[0].id : 0;
      const currentYear = new Date().getFullYear();
      setEditingGoal({ ...editingGoal, targets: [...(editingGoal.targets || []), { value: 0, targetYear: currentYear, deadline: '', unit: '', targetType: 'ABSOLUTE', indicatorId: defaultIndicatorId }] });
    }
  };
  const handleTargetChange = (idx: number, field: string, value: any) => {
    if (editingGoal) {
      const newTargets = [...(editingGoal.targets || [])];
      if (field === 'targetYear') {
        // Only store the year as a number
        newTargets[idx] = { ...newTargets[idx], targetYear: Number(value) };
      } else {
        newTargets[idx] = { ...newTargets[idx], [field]: value };
        // If targetType changes, reset value accordingly
        if (field === 'targetType') {
          newTargets[idx].value = 0; // Reset value for all types when type changes
        }
      }
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
          <FormControl fullWidth sx={{ mb: 2 }} required error={!editingGoal?.group && editingGoal?.id === 0}>
            <InputLabel>Goal Group *</InputLabel>
            <Select
              value={editingGoal?.group !== undefined ? String(editingGoal.group) : ''}
              onChange={e => {
                if (e.target.value === '__add_new__') {
                  setAddGroupModalOpen(true);
                } else {
                  setEditingGoal(editingGoal ? { ...editingGoal, group: Number(e.target.value) } : null);
                }
              }}
              input={<OutlinedInput label="Goal Group *" />}
              renderValue={selected => {
                if (selected === '' || selected === '__add_new__') return '';
                const group = groups.find(g => g.id === Number(selected));
                return group ? group.name : '';
              }}
            >
              {groups.map(group => (
                <MenuItem key={group.id} value={String(group.id)}>{group.name}</MenuItem>
              ))}
              <MenuItem value="__add_new__">
                <Box display="flex" alignItems="center">
                  <AddCircleOutlineIcon fontSize="small" sx={{ mr: 1 }} />
                  Add new group
                </Box>
              </MenuItem>
            </Select>
            {!editingGoal?.group && editingGoal?.id === 0 && (
              <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                Goal group is required
              </Typography>
            )}
          </FormControl>
          <TextField
            label="Year"
            type="number"
            value={editingGoal?.year || ''}
            onChange={e => setEditingGoal(editingGoal ? { ...editingGoal, year: Number(e.target.value) } : null)}
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
                  <FormControl fullWidth sx={{ mr: 2 }}>
                    <InputLabel>Target Type</InputLabel>
                    <Select
                      value={target.targetType || 'ABSOLUTE'}
                      onChange={e => handleTargetChange(idx, 'targetType', e.target.value)}
                      input={<OutlinedInput label="Target Type" />}
                    >
                      <MenuItem value="ABSOLUTE">Absolute Value</MenuItem>
                      <MenuItem value="RELATIVE">Relative Value</MenuItem>
                      <MenuItem value="PERCENTAGE_CHANGE">Percentage Change</MenuItem>
                    </Select>
                  </FormControl>
                  {target.targetType === 'PERCENTAGE_CHANGE' ? (
                    <TextField
                      label="Target Percentage (%)"
                      type="number"
                      value={target.value || ''}
                      onChange={e => handleTargetChange(idx, 'value', Number(e.target.value))}
                      InputLabelProps={{ shrink: true }}
                      sx={{ mr: 2 }}
                      inputProps={{ min: 0, max: 100 }}
                      required
                    />
                  ) : (
                    <TextField
                      label="Target Value"
                      type="number"
                      value={target.value || ''}
                      onChange={e => handleTargetChange(idx, 'value', Number(e.target.value))}
                      InputLabelProps={{ shrink: true }}
                      sx={{ mr: 2 }}
                      required
                    />
                  )}
                  <TextField
                    label="Target Year"
                    type="number"
                    value={target.targetYear || ''}
                    onChange={e => handleTargetChange(idx, 'targetYear', Number(e.target.value))}
                    InputLabelProps={{ shrink: true }}
                    sx={{ mr: 2 }}
                    required
                  />
                  <TextField
                    label="Deadline"
                    type="date"
                    value={target.deadline || ''}
                    onChange={e => handleTargetChange(idx, 'deadline', e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    sx={{ mr: 2 }}
                  />
                  <TextField
                    label="Unit"
                    value={target.unit || ''}
                    onChange={e => handleTargetChange(idx, 'unit', e.target.value)}
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
      {/* Add Group Modal */}
      <Dialog open={addGroupModalOpen} onClose={() => { setAddGroupModalOpen(false); setGroupCreateStatus('idle'); setGroupCreateError(null); }}>
        <DialogTitle>Add New Goal Group</DialogTitle>
        <DialogContent>
          <TextField
            label="Group Name"
            value={newGroupName}
            onChange={e => setNewGroupName(e.target.value)}
            fullWidth
            sx={{ mb: 2 }}
          />
          <TextField
            label="Description (optional)"
            value={newGroupDescription}
            onChange={e => setNewGroupDescription(e.target.value)}
            fullWidth
            sx={{ mb: 2 }}
          />
          {groupCreateStatus === 'success' && (
            <Alert severity="success">Goal group created successfully.</Alert>
          )}
          {groupCreateStatus === 'error' && (
            <Alert severity="error">{groupCreateError}</Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setAddGroupModalOpen(false); setGroupCreateStatus('idle'); setGroupCreateError(null); }}>Close</Button>
          <Button
            variant="contained"
            onClick={async () => {
              setGroupCreateStatus('idle');
              setGroupCreateError(null);
              try {
                const newGroup = await goalService.createGroup(newGroupName, newGroupDescription);
                setGroups(prev => [...prev, newGroup]);
                setGroupCreateStatus('success');
                setNewGroupName('');
                setNewGroupDescription('');
                // Select the new group in the goal form
                setEditingGoal(editingGoal ? { ...editingGoal, group: newGroup.id } : null);
              } catch (e: any) {
                setGroupCreateStatus('error');
                setGroupCreateError(e.message || 'Failed to create group');
              }
            }}
            disabled={!newGroupName.trim()}
          >
            Add
          </Button>
        </DialogActions>
      </Dialog>
      <Button
        variant="contained"
        color="primary"
        onClick={async () => {
          setSubmitting(true);
          setError(null);
          try {
            // Validate that all goals have goal groups
            const goalsWithoutGroups = goals.filter(goal => !(goal.group || goal.goalGroup?.id));
            if (goalsWithoutGroups.length > 0) {
              setError(`The following goals are missing goal groups: ${goalsWithoutGroups.map(g => g.name).join(', ')}`);
              setSubmitting(false);
              return;
            }
            
            // Submit all goals
            for (const goal of goals) {
              const goalGroupId = goal.group || goal.goalGroup?.id;
              const payload = { ...goal, goalGroupId: goalGroupId, year: goal.year };
              delete payload.group;
              delete payload.goalGroup;
              if (!goal.id || goal.id === 0) {
                await goalService.createGoal(payload);
              } else {
                await goalService.updateGoal(goal.id, payload);
              }
            }
            // Refresh
            const updatedGoals = await goalService.getGoals();
            setGoals(updatedGoals.map(g => ({ 
              year: (g as any).year ?? new Date().getFullYear(), 
              ...g,
              group: g.goalGroup?.id // Map goalGroup to group for consistency
            })));
            const updatedGroups = await goalService.getGroups();
            setGroups(updatedGroups);
          } catch (e: any) {
            setError(e.message || 'Failed to submit goals');
          } finally {
            setSubmitting(false);
          }
        }}
        disabled={submitting || loading}
        sx={{ mt: 3 }}
      >
        Submit Goals
      </Button>
    </Box>
  );
}; 