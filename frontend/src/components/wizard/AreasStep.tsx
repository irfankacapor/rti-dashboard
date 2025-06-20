import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Fab,
  Snackbar,
  Grid,
  CircularProgress
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { AreaCard } from './AreaCard';
import { useWizardStore } from '@/store/wizardStore';
import { Area, AreaFormData } from '@/types/areas';

const MAX_AREAS = 5;

export const AreasStep: React.FC = () => {
  const {
    areas,
    addArea,
    updateArea,
    deleteArea,
    canAddMoreAreas,
    fetchAreas
  } = useWizardStore();

  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [editArea, setEditArea] = useState<Area | null>(null);
  const [form, setForm] = useState<AreaFormData>({ name: '', description: '' });
  const [formError, setFormError] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Area | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);
  
  // Fetch initial data
  useEffect(() => {
    // Only fetch if areas are not loaded yet (i.e., empty or only default area)
    if (areas.length === 0 || (areas.length === 1 && areas[0].isDefault)) {
      fetchAreas();
    }
  }, [fetchAreas, areas]);

  // Filter out default area for UI
  const userAreas = areas.filter(a => !a.isDefault);

  // Handle add/edit dialog open
  const handleOpen = (area?: Area) => {
    if (area) {
      setEditArea(area);
      setForm({ name: area.name, description: area.description });
    } else {
      setEditArea(null);
      setForm({ name: '', description: '' });
    }
    setFormError(null);
    setOpen(true);
  };

  // Handle add/edit dialog close
  const handleClose = () => {
    setOpen(false);
    setEditArea(null);
    setForm({ name: '', description: '' });
    setFormError(null);
  };

  // Handle form change
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // Validate form
  const validate = (): string | null => {
    if (!form.name.trim()) return 'Area name is required.';
    if (
      userAreas.some(
        (a) =>
          a.name.trim().toLowerCase() === form.name.trim().toLowerCase() &&
          (!editArea || a.id !== editArea.id)
      )
    ) {
      return 'Area name must be unique.';
    }
    if (form.name.length > 100) return 'Area name is too long.';
    if (form.description.length > 300) return 'Description is too long.';
    return null;
  };

  // Handle form submit
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const error = validate();
    if (error) {
      setFormError(error);
      return;
    }

    setIsLoading(true);
    setFormError(null);

    try {
      if (editArea) {
        await updateArea(editArea.id, { name: form.name, description: form.description });
        setSnackbar('Area updated successfully');
      } else {
        await addArea({ name: form.name, description: form.description });
        setSnackbar('Area added successfully');
      }
      handleClose();
    } catch (err) {
      setFormError((err as Error).message);
    } finally {
      setIsLoading(false);
    }
  };

  // Handle delete
  const handleDelete = (area: Area) => {
    setDeleteConfirm(area);
  };
  
  const confirmDelete = async () => {
    if (deleteConfirm) {
      setIsLoading(true);
      try {
        await deleteArea(deleteConfirm.id);
        setSnackbar('Area deleted');
      } catch (err) {
        setSnackbar(`Error: ${(err as Error).message}`);
      } finally {
        setIsLoading(false);
        setDeleteConfirm(null);
      }
    }
  };

  // UI
  const showLimitWarning = userAreas.length >= MAX_AREAS - 1;
  const canAdd = userAreas.length < MAX_AREAS;
  const isEmpty = userAreas.length === 0;

  return (
    <Box>
      <Box mb={2}>
        <ul>
          <li>
            <Typography variant="body2">Create up to 5 main areas for your dashboard</Typography>
          </li>
          <li>
            <Typography variant="body2">Define names and descriptions for each area</Typography>
          </li>
          <li>
            <Typography variant="body2">Set up the foundation for your indicator groupings</Typography>
          </li>
        </ul>
      </Box>
      <Typography variant="h5" gutterBottom>
        Manage Areas
      </Typography>
      {showLimitWarning && (
        <Alert severity={userAreas.length === MAX_AREAS ? 'error' : 'warning'} sx={{ mb: 2 }} icon={<WarningAmberIcon />}>
          {userAreas.length === MAX_AREAS
            ? 'You have reached the maximum of 5 areas.'
            : `You are approaching the 5 areas limit (${userAreas.length}/5).`}
        </Alert>
      )}
      <Grid container spacing={2} sx={{ mt: 1 }}>
        {userAreas.map((area) => (
          <Grid item xs={12} sm={6} md={4} key={area.id}>
            <AreaCard area={area} onEdit={handleOpen} onDelete={handleDelete} />
          </Grid>
        ))}
      </Grid>
      {isEmpty && !isLoading && (
        <Box mt={4} textAlign="center">
          <Typography variant="body1" color="text.secondary">
            No areas found. Add your first area to get started.
          </Typography>
        </Box>
      )}
      {isLoading && <Box mt={4} textAlign="center"><CircularProgress /></Box>}
      <Box mt={3} display="flex" justifyContent="center">
        <Fab
          color="primary"
          aria-label="add"
          onClick={() => handleOpen()}
          disabled={!canAdd || isLoading}
          sx={{ position: 'relative' }}
        >
          <AddIcon />
        </Fab>
      </Box>
      {/* Add/Edit Dialog */}
      <Dialog open={open} onClose={handleClose} maxWidth="xs" fullWidth>
        <form onSubmit={handleSubmit}>
          <DialogTitle>{editArea ? 'Edit Area' : 'Add New Area'}</DialogTitle>
          <DialogContent>
            {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
            <TextField
              autoFocus
              margin="dense"
              name="name"
              label="Area Name"
              type="text"
              fullWidth
              value={form.name}
              onChange={handleChange}
              error={!!formError}
              inputProps={{ maxLength: 100 }}
              required
              disabled={isLoading}
            />
            <TextField
              margin="dense"
              name="description"
              label="Description (optional)"
              type="text"
              fullWidth
              value={form.description}
              onChange={handleChange}
              inputProps={{ maxLength: 300 }}
              multiline
              rows={3}
              disabled={isLoading}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose} disabled={isLoading}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={isLoading}>
              {isLoading ? <CircularProgress size={24} /> : (editArea ? 'Save Changes' : 'Create Area')}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete the area "{deleteConfirm?.name}"? This cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirm(null)} disabled={isLoading}>Cancel</Button>
          <Button onClick={confirmDelete} color="error" variant="contained" disabled={isLoading}>
            {isLoading ? <CircularProgress size={24} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
      <Snackbar
        open={!!snackbar}
        autoHideDuration={3000}
        onClose={() => setSnackbar(null)}
        message={snackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
}; 