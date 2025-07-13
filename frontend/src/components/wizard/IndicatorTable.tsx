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
  Button,
} from '@mui/material';

import { ManagedIndicator, UnitResponse } from '@/types/indicators';
import { Subarea } from '@/types/subareas';
import { IndicatorTableRow } from './IndicatorTableRow';
import { UnitPickerModal } from './UnitPickerModal';

interface IndicatorTableProps {
  indicators: ManagedIndicator[];
  subareas: any[];
  selectedIds: string[];
  onSelectionChange: (ids: string[]) => void;
  onIndicatorUpdate: (id: string, updates: Partial<ManagedIndicator>) => void;
  onIndicatorDelete: (id: string) => void;
  onIndicatorDeleteWithData: (id: string) => void;
  onBulkUpdate: (updates: { id: string; updates: Partial<ManagedIndicator> }[]) => void;
  isSaving: boolean;
  onEditValues: (indicatorId: string) => void;
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
  onEditValues,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [unitPickerModalOpen, setUnitPickerModalOpen] = useState<string | null>(null);
  const [unitPickerInitial, setUnitPickerInitial] = useState<{unit?: string, prefix?: string, suffix?: string}>({});

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

  const handleOpenUnitModal = (indicator: ManagedIndicator) => {
    setUnitPickerModalOpen(indicator.id);
    setUnitPickerInitial({
      unit: indicator.unit,
      prefix: indicator.unitPrefix,
      suffix: indicator.unitSuffix,
    });
  };

  const handleCloseUnitModal = () => {
    setUnitPickerModalOpen(null);
  };

  const handleSaveUnit = (unit: string, prefix: string, suffix: string) => {
    if (unitPickerModalOpen) {
      onIndicatorUpdate(unitPickerModalOpen, { unit, unitPrefix: prefix, unitSuffix: suffix });
      setUnitPickerModalOpen(null);
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
              onEditValues={onEditValues}
              onOpenUnitPicker={() => handleOpenUnitModal(indicator)}
            />
          ))}
        </TableBody>
      </Table>
      {unitPickerModalOpen && (
        <UnitPickerModal
          open={!!unitPickerModalOpen}
          initialUnit={unitPickerInitial.unit}
          initialPrefix={unitPickerInitial.prefix}
          initialSuffix={unitPickerInitial.suffix}
          onSave={handleSaveUnit}
          onClose={handleCloseUnitModal}
        />
      )}
    </TableContainer>
  );
}; 