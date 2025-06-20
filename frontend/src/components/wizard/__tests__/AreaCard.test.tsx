import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { AreaCard } from '../AreaCard';

const baseArea = {
  id: '1',
  name: 'Test Area',
  description: 'Test Description',
  code: 'test-code',
  isDefault: false,
  createdAt: new Date('2024-01-01T00:00:00Z'),
};

describe('AreaCard', () => {
  it('renders area name, description, and code', () => {
    render(<AreaCard area={baseArea} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.getByText('Test Area')).toBeInTheDocument();
    expect(screen.getByText('Test Description')).toBeInTheDocument();
    expect(screen.getByText('test-code')).toBeInTheDocument();
  });

  it('renders "Default Area" chip if isDefault', () => {
    render(<AreaCard area={{ ...baseArea, isDefault: true }} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.getByText('Default Area')).toBeInTheDocument();
  });

  it('renders createdAt date', () => {
    render(<AreaCard area={baseArea} onEdit={jest.fn()} onDelete={jest.fn()} />);
    // Should match the formatted date string
    expect(screen.getByText('1/1/2024')).toBeInTheDocument();
  });

  it('calls onEdit when edit button is clicked', () => {
    const onEdit = jest.fn();
    render(<AreaCard area={baseArea} onEdit={onEdit} onDelete={jest.fn()} />);
    fireEvent.click(screen.getByLabelText('edit'));
    expect(onEdit).toHaveBeenCalledWith(baseArea);
  });

  it('calls onDelete when delete button is clicked', () => {
    const onDelete = jest.fn();
    render(<AreaCard area={baseArea} onEdit={jest.fn()} onDelete={onDelete} />);
    fireEvent.click(screen.getByLabelText('delete'));
    expect(onDelete).toHaveBeenCalledWith(baseArea);
  });

  it('disables delete button for default area', () => {
    render(<AreaCard area={{ ...baseArea, isDefault: true }} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.getByLabelText('delete')).toBeDisabled();
  });

  it('shows fallback text if no description', () => {
    render(<AreaCard area={{ ...baseArea, description: '' }} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.getByText('No description provided.')).toBeInTheDocument();
  });
}); 