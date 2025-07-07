import React from 'react';
import { Table, TableHead, TableRow, TableCell, TableBody, TextField, Box, Typography } from '@mui/material';
import { IndicatorValueRow, IndicatorValueEdit } from '@/types/indicatorValues';

interface EditableValuesTableProps {
  data: IndicatorValueRow[];
  dimensions: string[];
  indicatorName: string;
  onValueChange: (factId: string, newValue: string) => void;
  editedValues: Record<string, IndicatorValueEdit>;
  validationErrors: Record<string, string>;
}

const EditableValuesTable: React.FC<EditableValuesTableProps> = ({
  data, dimensions, indicatorName, onValueChange, editedValues, validationErrors
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
          </TableRow>
        </TableHead>
        <TableBody>
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
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
      {data.length === 0 && (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          No data available for this indicator.
        </Typography>
      )}
    </Box>
  );
};

export default EditableValuesTable; 