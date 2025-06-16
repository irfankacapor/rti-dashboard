import { render, screen } from '@testing-library/react';
import { NextIntlClientProvider } from 'next-intl';
import Home from '../app/[locale]/page';

const messages = {
  Index: {
    title: 'Welcome to RTI Dashboard',
    description: 'A modern dashboard built with Next.js'
  }
};

describe('Home Page', () => {
  it('renders without crashing', () => {
    render(
      <NextIntlClientProvider messages={messages}>
        <Home />
      </NextIntlClientProvider>
    );
    
    expect(screen.getByText('Welcome to RTI Dashboard')).toBeInTheDocument();
    expect(screen.getByText('A modern dashboard built with Next.js')).toBeInTheDocument();
  });
}); 