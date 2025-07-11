'use client';
import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  Alert
} from '@mui/material';
import { CellSelection, DimensionMapping } from '@/types/csvProcessing';

type CsvTableProps = {
  data: string[][];
  onCellRangeSelect: (selection: CellSelection, event?: React.MouseEvent) => void;
  existingMappings: DimensionMapping[];
  maxDisplayRows?: number;
  maxDisplayCols?: number;
};

const SELECTION_COLORS = [
  '#e3f2fd', // Light blue
  '#f3e5f5', // Light purple
  '#e8f5e8', // Light green
  '#fff3e0', // Light orange
  '#fce4ec', // Light pink
  '#f1f8e9', // Light lime
  '#e0f2f1', // Light teal
  '#fafafa', // Light grey
];

export const CsvTable: React.FC<CsvTableProps> = ({
  data,
  onCellRangeSelect,
  existingMappings,
  maxDisplayRows = 20,
  maxDisplayCols = 15
}) => {
  const [isSelecting, setIsSelecting] = useState(false);
  const [selectionStart, setSelectionStart] = useState<{row: number, col: number} | null>(null);
  const [anchorCell, setAnchorCell] = useState<{row: number, col: number} | null>(null);
  const [currentSelection, setCurrentSelection] = useState<CellSelection | null>(null);
  const [showCoordinates, setShowCoordinates] = useState(false);
  const tableRef = useRef<HTMLDivElement>(null);

  // Preprocess data: do not hard-code header rows. For each row, keep leading empty cell if present, otherwise do not add one. Pad all rows at the end to the max row length.
  let processedData = data.map(row => [...row]);
  // Find the maximum row length
  const maxCols = Math.max(...processedData.map(row => row.length));
  // Pad all rows at the end to the max row length
  processedData = processedData.map(row => {
    const newRow = [...row];
    while (newRow.length < maxCols) {
      newRow.push('');
    }
    return newRow;
  });

  // Limit display size for performance
  const displayData = processedData.slice(0, maxDisplayRows).map(row => row.slice(0, maxDisplayCols));
  const hasMoreRows = data.length > maxDisplayRows;
  const hasMoreCols = data[0]?.length > maxDisplayCols;

  const generateId = () => `selection_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

  // Helper to build selection from two corners
  const buildSelection = (start: {row: number, col: number}, end: {row: number, col: number}) => {
    const selection: CellSelection = {
      startRow: Math.min(start.row, end.row),
      endRow: Math.max(start.row, end.row),
      startCol: Math.min(start.col, end.col),
      endCol: Math.max(start.col, end.col),
      selectedCells: [],
      selectionId: generateId()
    };
    for (let r = selection.startRow; r <= selection.endRow; r++) {
      for (let c = selection.startCol; c <= selection.endCol; c++) {
        if (displayData[r] && displayData[r][c] !== undefined) {
          selection.selectedCells.push({ row: r, col: c, value: displayData[r][c] });
        }
      }
    }
    return selection;
  };

  // Mouse down: start selection and set anchor
  const handleMouseDown = (row: number, col: number, event?: React.MouseEvent) => {
    if (event && event.shiftKey && anchorCell) {
      // Shift+Click: extend selection from anchorCell to clicked cell
      const selection = buildSelection(anchorCell, { row, col });
      setCurrentSelection(selection);
      setIsSelecting(false);
      setSelectionStart(null);
    } else {
      setIsSelecting(true);
      setSelectionStart({ row, col });
      setAnchorCell({ row, col });
      setCurrentSelection({
        startRow: row,
        endRow: row,
        startCol: col,
        endCol: col,
        selectedCells: [{ row, col, value: displayData[row][col] }],
        selectionId: generateId()
      });
    }
  };

  // Mouse enter: drag selection
  const handleMouseEnter = (row: number, col: number) => {
    if (isSelecting && selectionStart) {
      const selection = buildSelection(selectionStart, { row, col });
      setCurrentSelection(selection);
    }
  };

  // Mouse up: finish selection
  const handleMouseUp = () => {
    if (currentSelection && isSelecting) {
      onCellRangeSelect(currentSelection);
    }
    setIsSelecting(false);
    setSelectionStart(null);
  };

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.shiftKey && e.metaKey && currentSelection && anchorCell) {
        e.preventDefault();
        let newSelection = { ...currentSelection };
        let anchor = anchorCell;
        switch (e.key) {
          case 'ArrowUp':
            if (currentSelection.startRow === 0 && currentSelection.endRow === anchor.row) {
              // Already at edge, do nothing
              break;
            }
            if (currentSelection.startRow !== anchor.row || currentSelection.endRow !== anchor.row) {
              // Shrink back to anchor
              newSelection = buildSelection(anchor, anchor);
            } else {
              // Extend to edge
              newSelection = buildSelection(anchor, { row: 0, col: anchor.col });
            }
            break;
          case 'ArrowDown':
            if (currentSelection.endRow === displayData.length - 1 && currentSelection.startRow === anchor.row) {
              break;
            }
            if (currentSelection.startRow !== anchor.row || currentSelection.endRow !== anchor.row) {
              newSelection = buildSelection(anchor, anchor);
            } else {
              newSelection = buildSelection(anchor, { row: displayData.length - 1, col: anchor.col });
            }
            break;
          case 'ArrowLeft':
            if (currentSelection.startCol === 0 && currentSelection.endCol === anchor.col) {
              break;
            }
            if (currentSelection.startCol !== anchor.col || currentSelection.endCol !== anchor.col) {
              newSelection = buildSelection(anchor, anchor);
            } else {
              newSelection = buildSelection(anchor, { row: anchor.row, col: 0 });
            }
            break;
          case 'ArrowRight':
            if (currentSelection.endCol === displayData[0]?.length - 1 && currentSelection.startCol === anchor.col) {
              break;
            }
            if (currentSelection.startCol !== anchor.col || currentSelection.endCol !== anchor.col) {
              newSelection = buildSelection(anchor, anchor);
            } else {
              newSelection = buildSelection(anchor, { row: anchor.row, col: displayData[0]?.length - 1 });
            }
            break;
        }
        setCurrentSelection(newSelection);
      }
      
      // Toggle coordinate display with Cmd/Ctrl + C
      if ((e.metaKey || e.ctrlKey) && e.key === 'c') {
        e.preventDefault();
        setShowCoordinates(!showCoordinates);
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [currentSelection, displayData, anchorCell, setCurrentSelection, setShowCoordinates, showCoordinates]);

  const getCellClassName = (row: number, col: number): string => {
    const classes = ['csv-table-cell'];
    
    // Check if cell is in current selection
    if (currentSelection) {
      const isInCurrentSelection = currentSelection.selectedCells.some(
        cell => cell.row === row && cell.col === col
      );
      if (isInCurrentSelection) {
        classes.push('current-selection');
      }
    }
    
    // Check if cell is in existing mappings
    existingMappings.forEach((mapping, index) => {
      const isInMapping = mapping.selection.selectedCells.some(
        cell => cell.row === row && cell.col === col
      );
      if (isInMapping) {
        classes.push(`mapping-${index}`);
      }
    });
    
    return classes.join(' ');
  };

  const getCellStyle = (row: number, col: number): React.CSSProperties => {
    const style: React.CSSProperties = {
      position: 'relative',
      cursor: 'pointer',
      userSelect: 'none',
      minWidth: '100px',
      maxWidth: '200px',
      wordBreak: 'break-word'
    };
    
    // Current selection highlighting
    if (currentSelection) {
      const isInCurrentSelection = currentSelection.selectedCells.some(
        cell => cell.row === row && cell.col === col
      );
      if (isInCurrentSelection) {
        style.backgroundColor = '#2196f3';
        style.color = 'white';
      }
    }
    
    // Existing mapping highlighting
    existingMappings.forEach((mapping, index) => {
      const isInMapping = mapping.selection.selectedCells.some(
        cell => cell.row === row && cell.col === col
      );
      if (isInMapping) {
        style.backgroundColor = SELECTION_COLORS[index % SELECTION_COLORS.length];
        style.border = `2px solid ${mapping.color}`;
      }
    });
    
    return style;
  };

  if (!data || data.length === 0) {
    return (
      <Alert severity="info">
        No CSV data available. Please upload a CSV file first.
      </Alert>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">
          CSV Data Preview
        </Typography>
        <Box display="flex" gap={1}>
          <Chip 
            label={`${data.length} rows × ${data[0]?.length || 0} columns`}
            size="small"
            variant="outlined"
          />
          <Chip 
            label={showCoordinates ? 'Hide Coordinates' : 'Show Coordinates'}
            size="small"
            variant="outlined"
            onClick={() => setShowCoordinates(!showCoordinates)}
          />
        </Box>
      </Box>

      {(hasMoreRows || hasMoreCols) && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Showing {maxDisplayRows} of {data.length} rows and {maxDisplayCols} of {data[0]?.length || 0} columns.
          {hasMoreRows && ' Use cell selection to map larger datasets.'}
        </Alert>
      )}

      <Paper 
        ref={tableRef}
        className={isSelecting ? 'csv-table-noselect' : ''}
        sx={{ overflow: 'auto', maxHeight: '600px' }}
      >
        <TableContainer>
          <Table size="small" stickyHeader>
            <TableHead>
              <TableRow>
                {showCoordinates && (
                  <TableCell 
                    sx={{ 
                      backgroundColor: 'grey.100', 
                      fontWeight: 'bold',
                      minWidth: '80px'
                    }}
                  >
                    Coord
                  </TableCell>
                )}
                {displayData[0]?.map((_, colIndex) => (
                  <TableCell 
                    key={colIndex}
                    sx={{ 
                      backgroundColor: 'grey.100', 
                      fontWeight: 'bold',
                      minWidth: '100px'
                    }}
                  >
                    {showCoordinates ? `Col ${colIndex}` : `Column ${colIndex + 1}`}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {displayData.map((row, rowIndex) => (
                <TableRow key={rowIndex}>
                  {showCoordinates && (
                    <TableCell 
                      sx={{ 
                        backgroundColor: 'grey.50', 
                        fontWeight: 'bold',
                        fontSize: '0.75rem'
                      }}
                    >
                      Row {rowIndex}
                    </TableCell>
                  )}
                  {row.map((cell, colIndex) => (
                    <TableCell
                      key={colIndex}
                      onMouseDown={(e) => handleMouseDown(rowIndex, colIndex, e)}
                      onMouseEnter={() => handleMouseEnter(rowIndex, colIndex)}
                      onMouseUp={(e) => {
                        if (isSelecting && currentSelection) {
                          onCellRangeSelect(currentSelection, e);
                        }
                        setIsSelecting(false);
                        setSelectionStart(null);
                      }}
                      className={getCellClassName(rowIndex, colIndex)}
                      style={getCellStyle(rowIndex, colIndex)}
                    >
                      <Typography variant="body2" noWrap style={{ userSelect: 'none' }}>
                        {cell || ''}
                      </Typography>
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Box mt={2}>
        <Typography variant="body2" color="text.secondary">
          <strong>Instructions:</strong> Click and drag to select cells, or use Shift+Cmd+Arrow keys to extend selection. 
          Press Cmd+C to toggle coordinate display.
        </Typography>
      </Box>

      {existingMappings.length > 0 && (
        <Box mt={2}>
          <Typography variant="subtitle2" gutterBottom>
            Existing Mappings:
          </Typography>
          <Box display="flex" gap={1} flexWrap="wrap">
            {existingMappings.map((mapping, index) => (
              <Chip
                key={mapping.id}
                label={`${mapping.dimensionType}${mapping.subType ? ` (${mapping.subType})` : ''}`}
                size="small"
                sx={{
                  backgroundColor: SELECTION_COLORS[index % SELECTION_COLORS.length],
                  border: `1px solid ${mapping.color}`
                }}
              />
            ))}
          </Box>
        </Box>
      )}
    </Box>
  );
}; 