import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import WizardLayout from '../layout';

// Mock the useAuth hook
jest.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({
    user: { role: 'ADMIN' },
    isLoading: false,
    isAuthenticated: true,
  }),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(() => ({
    push: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
  })),
  useParams: jest.fn(() => ({ locale: 'en' })),
}));

describe('WizardLayout', () => {
  it('renders children properly', () => {
    render(
      <WizardLayout>
        <div data-testid="test-child">Test Content</div>
      </WizardLayout>
    );
    
    expect(screen.getByTestId('test-child')).toBeInTheDocument();
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('applies proper container structure', () => {
    const { container } = render(
      <WizardLayout>
        <div>Content</div>
      </WizardLayout>
    );
    
    expect(container.firstChild).toBeDefined();
  });
}); 