import React, { useState } from 'react';
import { 
  Table, 
  TableHead, 
  TableRow, 
  TableCell, 
  TableBody, 
  TextField, 
  Box, 
  Typography, 
  Button, 
  IconButton,
  Tooltip
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { IndicatorValueRow, IndicatorValueEdit, NewIndicatorValueRow } from '@/types/indicatorValues';

interface EditableValuesTableProps {
  data: IndicatorValueRow[];
  dimensions: string[];
  indicatorName: string;
  onValueChange: (factId: string, newValue: string) => void;
  onNewRowChange: (tempId: string, field: string, value: string) => void;
  onAddNewRow: () => void;
  onRemoveNewRow: (tempId: string) => void;
  editedValues: Record<string, IndicatorValueEdit>;
  newRows: NewIndicatorValueRow[];
  validationErrors: Record<string, string>;
}

const EditableValuesTable: React.FC<EditableValuesTableProps> = ({
  data, 
  dimensions, 
  indicatorName, 
  onValueChange, 
  onNewRowChange,
  onAddNewRow,
  onRemoveNewRow,
  editedValues, 
  newRows,
  validationErrors
}) => {
  return (
    <Box sx={{ overflowX: 'auto' }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            {dimensions.map(dim => (
              <TableCell key={dim}>{dim}</TableCell>
            ))}
            <TableCell>{indicatorName}</TableCell>
            <TableCell width={50}>
              <Tooltip title="Add new row">
                <IconButton 
                  size="small" 
                  onClick={onAddNewRow}
                  color="primary"
                >
                  <AddIcon />
                </IconButton>
              </Tooltip>
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {/* Existing rows */}
          {data.map(row => {
            const edit = editedValues[row.factId];
            const isEdited = !!edit && edit.newValue !== row.value;
            const isEmpty = row.isEmpty;
            const error = validationErrors[row.factId];
            return (
              <TableRow key={row.factId}>
                {dimensions.map(dim => (
                  <TableCell key={dim}>{row.dimensions[dim] || ''}</TableCell>
                ))}
                <TableCell>
                  <TextField
                    value={edit ? edit.newValue : row.value ?? ''}
                    onChange={e => onValueChange(row.factId, e.target.value)}
                    size="small"
                    error={!!error}
                    helperText={error}
                    sx={{
                      backgroundColor: error
                        ? 'rgba(255,0,0,0.08)'
                        : isEdited
                        ? 'rgba(0,255,0,0.08)'
                        : isEmpty
                        ? 'rgba(255,165,0,0.12)'
                        : undefined,
                      minWidth: 100,
                    }}
                  />
                </TableCell>
                <TableCell></TableCell>
              </TableRow>
            );
          })}
          
          {/* New rows */}
          {newRows.map(newRow => {
            const error = validationErrors[newRow.tempId];
            return (
              <TableRow key={newRow.tempId} sx={{ backgroundColor: 'rgba(0,255,0,0.05)' }}>
                {dimensions.map(dim => (
                  <TableCell key={dim}>
                    <TextField
                      value={newRow.dimensions[dim] || ''}
                      onChange={e => onNewRowChange(newRow.tempId, dim, e.target.value)}
                      size="small"
                      placeholder={`Enter ${dim}`}
                      sx={{ minWidth: 120 }}
                    />
                  </TableCell>
                ))}
                <TableCell>
                  <TextField
                    value={newRow.value || ''}
                    onChange={e => onNewRowChange(newRow.tempId, 'value', e.target.value)}
                    size="small"
                    type="number"
                    error={!!error}
                    helperText={error}
                    placeholder="Enter value"
                    sx={{
                      backgroundColor: error ? 'rgba(255,0,0,0.08)' : undefined,
                      minWidth: 100,
                    }}
                  />
                </TableCell>
                <TableCell>
                  <Tooltip title="Remove new row">
                    <IconButton 
                      size="small" 
                      onClick={() => onRemoveNewRow(newRow.tempId)}
                      color="error"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
      {data.length === 0 && newRows.length === 0 && (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          No data available for this indicator. Use the + button to add new rows.
        </Typography>
      )}
    </Box>
  );
};

export default EditableValuesTable; 