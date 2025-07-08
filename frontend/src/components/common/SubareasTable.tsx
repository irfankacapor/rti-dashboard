import React, { useState } from 'react';
import {
  Table, TableHead, TableBody, TableRow, TableCell,
  IconButton, TextField, Select, MenuItem, Button, Chip
} from '@mui/material';
import { SelectChangeEvent } from '@mui/material/Select';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { Subarea, SubareaFormData } from '@/types/subareas';
import { Area } from '@/types/areas';
import { ManagedIndicator } from '@/types/indicators';
import SubareaDeleteWarningModal from '../SubareaDeleteWarningModal';

interface SubareasTableProps {
  subareas: Subarea[];
  areas: Area[];
  allAreas: Area[];
  onAdd: (subarea: SubareaFormData) => void;
  onUpdate: (id: string, updates: Partial<Subarea>) => void;
  onDelete: (id: string) => void;
  onDeleteWithData?: (id: string) => void;
  getRelatedIndicators?: (subareaId: string) => ManagedIndicator[];
  showAreaColumn?: boolean;
  isWizardMode?: boolean;
  allowEdit?: boolean;
}

export const SubareasTable: React.FC<SubareasTableProps> = ({
  subareas = [],
  areas = [],
  allAreas = [],
  onAdd,
  onUpdate,
  onDelete,
  onDeleteWithData,
  getRelatedIndicators,
  showAreaColumn = false,
  isWizardMode = false,
  allowEdit = true,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<SubareaFormData>({ name: '', description: '', areaId: '' });
  const [addMode, setAddMode] = useState(false);
  const [addForm, setAddForm] = useState<SubareaFormData>({ name: '', description: '', areaId: '' });
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [showWarningModal, setShowWarningModal] = useState(false);
  const [subareaToDelete, setSubareaToDelete] = useState<Subarea | null>(null);

  const handleEdit = (subarea: Subarea) => {
    setEditingId(subarea.id);
    setEditForm({ name: subarea.name, description: subarea.description, areaId: subarea.areaId });
    setError(null);
  };
  const handleEditTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEditForm((f) => ({ ...f, [name]: value }));
  };
  const handleEditSelectChange = (e: SelectChangeEvent) => {
    const { name, value } = e.target;
    setEditForm((f) => ({ ...f, [name as string]: value as string }));
  };
  const handleEditSave = () => {
    if (!editForm.name.trim()) {
      setError('Name is required');
      return;
    }
    onUpdate(editingId!, editForm);
    setEditingId(null);
    setError(null);
  };
  const handleEditCancel = () => {
    setEditingId(null);
    setError(null);
  };
  const handleAddTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setAddForm((f) => ({ ...f, [name]: value }));
  };
  const handleAddSelectChange = (e: SelectChangeEvent) => {
    const { name, value } = e.target;
    setAddForm((f) => ({ ...f, [name as string]: value as string }));
  };
  const handleAddSave = () => {
    if (!addForm.name.trim()) {
      setError('Name is required');
      return;
    }
    onAdd(addForm);
    setAddMode(false);
    setAddForm({ name: '', description: '', areaId: '' });
    setError(null);
  };
  const handleAddCancel = () => {
    setAddMode(false);
    setAddForm({ name: '', description: '', areaId: '' });
    setError(null);
  };
  const handleDelete = (id: string) => {
    const subarea = subareas.find(s => s.id === id);
    if (subarea) {
      setSubareaToDelete(subarea);
      setShowWarningModal(true);
    }
  };

  const handleDeleteConfirm = async () => {
    if (subareaToDelete) {
      try {
        await onDelete(subareaToDelete.id);
      } catch (error) {
        setError(error instanceof Error ? error.message : 'Failed to delete subarea');
      }
    }
    setShowWarningModal(false);
    setSubareaToDelete(null);
  };

  const handleDeleteWithData = async () => {
    if (subareaToDelete && onDeleteWithData) {
      try {
        await onDeleteWithData(subareaToDelete.id);
      } catch (error) {
        setError(error instanceof Error ? error.message : 'Failed to delete subarea with data');
      }
    }
    setShowWarningModal(false);
    setSubareaToDelete(null);
  };

  const handleWarningModalClose = () => {
    setShowWarningModal(false);
    setSubareaToDelete(null);
  };
  // Ensure subareas is always an array
  const safeSubareas = Array.isArray(subareas) ? subareas : [];
  return (
    <>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Description</TableCell>
            {showAreaColumn && areas.length > 0 ? <TableCell data-testid="area-column-header">Area</TableCell> : null}
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {safeSubareas.map((sub) => (
            <TableRow key={sub.id}>
              <TableCell>
                {editingId === sub.id ? (
                  <TextField
                    name="name"
                    value={editForm.name}
                    onChange={handleEditTextChange}
                    size="small"
                    required
                    error={!!error && !editForm.name.trim()}
                    helperText={!!error && !editForm.name.trim() ? error : ''}
                  />
                ) : (
                  sub.name
                )}
              </TableCell>
              <TableCell>
                {editingId === sub.id ? (
                  <TextField
                    name="description"
                    value={editForm.description}
                    onChange={handleEditTextChange}
                    size="small"
                    multiline
                    minRows={1}
                  />
                ) : (
                  sub.description
                )}
              </TableCell>
              {showAreaColumn && areas.length > 0 && (
                <TableCell>
                  {editingId === sub.id ? (
                    <Select
                      name="areaId"
                      value={editForm.areaId}
                      onChange={handleEditSelectChange}
                      size="small"
                      required
                    >
                      {areas.map((a) => (
                        <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                      ))}
                    </Select>
                  ) : (
                    sub.areaId
                      ? (
                          allAreas.find((a) => a.id === sub.areaId)
                            ? <Chip label={allAreas.find((a) => a.id === sub.areaId)?.name} size="small" />
                            : <Chip label="Unassigned" size="small" disabled />
                        )
                      : null
                  )}
                </TableCell>
              )}
              <TableCell>
                {editingId === sub.id ? (
                  <>
                    <IconButton onClick={handleEditSave} color="primary" size="small" data-testid="save-subarea"><SaveIcon /></IconButton>
                    <IconButton onClick={handleEditCancel} size="small"><CancelIcon /></IconButton>
                  </>
                ) : (
                  <>
                    {allowEdit && (
                      <IconButton onClick={() => handleEdit(sub)} size="small" data-testid="edit-subarea"><EditIcon /></IconButton>
                    )}
                    {allowEdit && (
                      <IconButton onClick={() => handleDelete(sub.id)} color="error" size="small" data-testid="delete-subarea"><DeleteIcon /></IconButton>
                    )}
                  </>
                )}
              </TableCell>
            </TableRow>
          ))}
          {addMode && (
            <TableRow>
              <TableCell>
                <TextField
                  name="name"
                  value={addForm.name}
                  onChange={handleAddTextChange}
                  size="small"
                  required
                  error={!!error && !addForm.name.trim()}
                  helperText={!!error && !addForm.name.trim() ? error : ''}
                />
              </TableCell>
              <TableCell>
                <TextField
                  name="description"
                  value={addForm.description}
                  onChange={handleAddTextChange}
                  size="small"
                  multiline
                  minRows={1}
                />
              </TableCell>
              {showAreaColumn && areas.length > 0 && (
                <TableCell>
                  <Select
                    name="areaId"
                    value={addForm.areaId}
                    onChange={handleAddSelectChange}
                    size="small"
                    required
                    inputProps={{ 'data-testid': 'add-area-select' }}
                  >
                    {areas.map((a) => (
                      <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                    ))}
                  </Select>
                </TableCell>
              )}
              <TableCell>
                <IconButton onClick={handleAddSave} color="primary" size="small" data-testid="add-save-subarea"><SaveIcon /></IconButton>
                <IconButton onClick={handleAddCancel} size="small" data-testid="add-cancel-subarea"><CancelIcon /></IconButton>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
      {allowEdit && !addMode && (
        <Button onClick={() => setAddMode(true)} variant="outlined" sx={{ mt: 2 }}>
          Add Subarea
        </Button>
      )}
      {/* Warning modal for subarea deletion */}
      <SubareaDeleteWarningModal
        open={showWarningModal}
        onClose={handleWarningModalClose}
        subarea={subareaToDelete}
        relatedIndicators={getRelatedIndicators ? getRelatedIndicators(subareaToDelete?.id || '') : []}
        onDelete={handleDeleteConfirm}
        onDeleteWithData={handleDeleteWithData}
      />
    </>
  );
}; 