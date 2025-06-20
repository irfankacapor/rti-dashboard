import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { SubareasStep } from '../SubareasStep';
import { useWizardStore } from '@/store/wizardStore';
import { act } from 'react-dom/test-utils';

jest.mock('@/store/wizardStore');

const mockAreas = [
  { id: '1', name: 'Default Area', isDefault: true, description: '', code: '' },
  { id: '2', name: 'Area 2', isDefault: false, description: '', code: '' },
];
const mockSubareas = [
  { id: 's1', code: '', name: 'Subarea 1', description: '', areaId: '1', createdAt: new Date() },
];

// Helper to create a mock store state
const getMockState = (overrides = {}) => ({
  areas: mockAreas,
  subareas: mockSubareas,
  addSubarea: jest.fn(),
  updateSubarea: jest.fn(),
  deleteSubarea: jest.fn(),
  getDefaultAreaId: () => '1',
  ...overrides,
});

describe('SubareasStep', () => {
  beforeEach(() => {
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState()));
  });

  it('renders subareas table and area column when multiple areas', () => {
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState()));
    render(<SubareasStep />);
    expect(screen.getByText('Manage Subareas')).toBeInTheDocument();
    expect(screen.getByTestId('area-column-header')).toBeInTheDocument();
  });

  it('hides area column when only default area exists', () => {
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ areas: [mockAreas[0]] })));
    render(<SubareasStep />);
    expect(screen.queryByText('Area')).not.toBeInTheDocument();
  });

  it('shows validation warning if no subareas', () => {
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ subareas: [] })));
    render(<SubareasStep />);
    expect(screen.getByText(/must add at least one subarea/i)).toBeInTheDocument();
  });

  it('shows no area picker when only default area exists, and assigns subareas to default area', () => {
    const addSubarea = jest.fn();
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ areas: [mockAreas[0]], subareas: [], addSubarea })));
    const { container } = render(<SubareasStep />);
    fireEvent.click(screen.getByText(/add subarea/i));
    expect(container.querySelector('[data-testid="add-area-select"]')).toBeNull();
    fireEvent.change(screen.getAllByRole('textbox')[0], { target: { value: 'New Subarea' } });
    fireEvent.click(screen.getByTestId('add-save-subarea'));
    expect(addSubarea).toHaveBeenCalledWith(expect.objectContaining({ areaId: '1' }));
  });

  it('shows area picker when there are manual areas and allows picking', () => {
    const addSubarea = jest.fn();
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ addSubarea, subareas: [] })));
    const { container } = render(<SubareasStep />);
    fireEvent.click(screen.getByText(/add subarea/i));
    const areaSelect = container.querySelector('[data-testid="add-area-select"]');
    expect(areaSelect).not.toBeNull();
    fireEvent.change(screen.getAllByRole('textbox')[0], { target: { value: 'New Subarea' } });
    fireEvent.change(areaSelect as Element, { target: { value: mockAreas[1].id } });
    fireEvent.click(screen.getByTestId('add-save-subarea'));
    expect(addSubarea).toHaveBeenCalledWith(expect.objectContaining({ areaId: mockAreas[1].id }));
  });

  it('shows "Unassigned" chip if subarea areaId does not match any area', () => {
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ subareas: [{ ...mockSubareas[0], areaId: 'nonexistent' }] })));
    render(<SubareasStep />);
    expect(screen.getByText('Unassigned')).toBeInTheDocument();
  });

  it('calls updateSubarea when editing a subarea', async () => {
    const updateSubarea = jest.fn();
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ updateSubarea })));
    render(<SubareasStep />);
    fireEvent.click(await screen.findByTestId('edit-subarea'));
    fireEvent.change(screen.getAllByRole('textbox')[0], { target: { value: 'Edited Subarea' } });
    fireEvent.click(await screen.findByTestId('save-subarea'));
    expect(updateSubarea).toHaveBeenCalled();
  });

  it('calls deleteSubarea when deleting a subarea', async () => {
    const deleteSubarea = jest.fn();
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ deleteSubarea })));
    render(<SubareasStep />);
    fireEvent.click(await screen.findByTestId('delete-subarea'));
    fireEvent.click(screen.getByText('Delete'));
    expect(deleteSubarea).toHaveBeenCalled();
  });

  it('reassigns subareas to first manual area when manual area is added after subareas exist', () => {
    const updateSubarea = jest.fn();
    // Start with only default area and a subarea assigned to it
    let areas = [mockAreas[0]];
    let subareas = [{ ...mockSubareas[0], areaId: '1' }];
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ areas, subareas, updateSubarea })));
    // Render with only default area
    const { rerender } = render(<SubareasStep />);
    // Add a manual area
    areas = [mockAreas[0], mockAreas[1]];
    ((useWizardStore as unknown) as jest.Mock).mockImplementation((selector) => selector(getMockState({ areas, subareas, updateSubarea })));
    rerender(<SubareasStep />);
    // Should call updateSubarea to reassign to first manual area
    expect(updateSubarea).toHaveBeenCalledWith(subareas[0].id, { areaId: mockAreas[1].id });
  });
}); 