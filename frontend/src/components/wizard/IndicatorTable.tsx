'use client';
import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Checkbox,
  IconButton,
  Chip,
  Tooltip,
  Box,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  TextField,
  Button,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { ManagedIndicator } from '@/types/indicators';
import { Subarea } from '@/types/subareas';
import { IndicatorTableRow } from './IndicatorTableRow';

interface IndicatorTableProps {
  indicators: ManagedIndicator[];
  subareas: Subarea[];
  selectedIds: string[];
  onSelectionChange: (selectedIds: string[]) => void;
  onIndicatorUpdate: (id: string, updates: Partial<ManagedIndicator>) => void;
  onIndicatorDelete: (id: string) => void;
  onIndicatorDeleteWithData?: (id: string) => void;
  onBulkUpdate: (updates: { id: string; updates: Partial<ManagedIndicator> }[]) => void;
  isSaving: boolean;
}

export const IndicatorTable: React.FC<IndicatorTableProps> = ({
  indicators,
  subareas,
  selectedIds,
  onSelectionChange,
  onIndicatorUpdate,
  onIndicatorDelete,
  onIndicatorDeleteWithData,
  onBulkUpdate,
  isSaving,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      onSelectionChange(indicators.map(i => i.id));
    } else {
      onSelectionChange([]);
    }
  };

  const handleSelect = (id: string, selected: boolean) => {
    if (selected) {
      onSelectionChange([...selectedIds, id]);
    } else {
      onSelectionChange(selectedIds.filter(selectedId => selectedId !== id));
    }
  };

  const handleEdit = (id: string) => {
    setEditingId(id);
  };

  const handleSave = (id: string, data: Partial<ManagedIndicator>) => {
    onIndicatorUpdate(id, data);
    setEditingId(null);
  };

  const handleCancel = () => {
    setEditingId(null);
  };

  const handleDelete = (id: string) => {
    onIndicatorDelete(id);
  };

  const handleDeleteWithData = (id: string) => {
    if (onIndicatorDeleteWithData) {
      onIndicatorDeleteWithData(id);
    }
  };

  const isAllSelected = indicators.length > 0 && selectedIds.length === indicators.length;
  const isIndeterminate = selectedIds.length > 0 && selectedIds.length < indicators.length;

  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell padding="checkbox">
              <Checkbox
                checked={isAllSelected}
                indeterminate={isIndeterminate}
                onChange={handleSelectAll}
                disabled={isSaving}
              />
            </TableCell>
            <TableCell>Indicator Name</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Unit</TableCell>
            <TableCell>Source</TableCell>
            <TableCell>Data Type</TableCell>
            <TableCell>Subarea</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Values</TableCell>
            <TableCell>Dimensions</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {indicators.map(indicator => (
            <IndicatorTableRow
              key={indicator.id}
              indicator={indicator}
              isEditing={editingId === indicator.id}
              isSelected={selectedIds.includes(indicator.id)}
              subareas={subareas}
              onEdit={() => handleEdit(indicator.id)}
              onSave={(data) => handleSave(indicator.id, data)}
              onCancel={handleCancel}
              onSelect={(selected) => handleSelect(indicator.id, selected)}
              onDelete={() => handleDelete(indicator.id)}
              onDeleteWithData={() => handleDeleteWithData(indicator.id)}
              isSaving={isSaving}
            />
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}; 