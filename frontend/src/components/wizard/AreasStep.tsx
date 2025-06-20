import React, { useState } from 'react';
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
  } = useWizardStore();

  const [open, setOpen] = useState(false);
  const [editArea, setEditArea] = useState<Area | null>(null);
  const [form, setForm] = useState<AreaFormData>({ name: '', description: '' });
  const [formError, setFormError] = useState<string | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Area | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);

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
      areas.some(
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
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const error = validate();
    if (error) {
      setFormError(error);
      return;
    }
    if (editArea) {
      updateArea(editArea.id, { name: form.name, description: form.description });
      setSnackbar('Area updated');
    } else {
      addArea({ name: form.name, description: form.description, code: '', isDefault: false });
      setSnackbar('Area added');
    }
    handleClose();
  };

  // Handle delete
  const handleDelete = (area: Area) => {
    setDeleteConfirm(area);
  };
  const confirmDelete = () => {
    if (deleteConfirm) {
      deleteArea(deleteConfirm.id);
      setSnackbar('Area deleted');
    }
    setDeleteConfirm(null);
  };

  // UI
  const showLimitWarning = userAreas.length >= MAX_AREAS - 1;
  const canAdd = userAreas.length < MAX_AREAS;
  const isEmpty = userAreas.length === 0;

  return (
    <Box>
      {/* Step content list as described in the tests */}
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
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={area.id}>
            <AreaCard area={area} onEdit={handleOpen} onDelete={handleDelete} />
          </Grid>
        ))}
      </Grid>
      {isEmpty && (
        <Box mt={4} textAlign="center">
          <Typography variant="body1" color="text.secondary">
            Add your first area to get started.
          </Typography>
        </Box>
      )}
      <Box mt={3} display="flex" justifyContent="center">
        <Fab
          color="primary"
          aria-label="add"
          onClick={() => handleOpen()}
          disabled={!canAdd}
          sx={{ position: 'relative' }}
        >
          <AddIcon />
        </Fab>
      </Box>
      {/* Add/Edit Dialog */}
      <Dialog open={open} onClose={handleClose} maxWidth="xs" fullWidth>
        <form onSubmit={handleSubmit}>
          <DialogTitle>{editArea ? 'Edit Area' : 'Add Area'}</DialogTitle>
          <DialogContent>
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
              helperText={formError}
              inputProps={{ maxLength: 100 }}
              required
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
              minRows={2}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>Cancel</Button>
            <Button type="submit" variant="contained">
              {editArea ? 'Save' : 'Add'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)}>
        <DialogTitle>Delete Area</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete the area "{deleteConfirm?.name}"?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirm(null)}>Cancel</Button>
          <Button onClick={confirmDelete} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
      {/* Snackbar */}
      <Snackbar
        open={!!snackbar}
        autoHideDuration={2500}
        onClose={() => setSnackbar(null)}
        message={snackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
}; 