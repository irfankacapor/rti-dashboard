import React from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { NextIntlClientProvider } from 'next-intl';
import { ThemeProvider, createTheme } from '@mui/material/styles';

const theme = createTheme();

const messages = {
  Landing: {
    title: 'RTI Dashboard',
    subtitle: 'Create comprehensive indicator dashboards',
    startButton: 'Start Configuration'
  },
  Wizard: {
    title: 'Dashboard Configuration Wizard',
    step1: 'Areas Management'
  }
};

const AllTheProviders = ({ children }: { children: React.ReactNode }) => {
  return (
    <NextIntlClientProvider messages={messages} locale="en">
      <ThemeProvider theme={theme}>
        {children}
      </ThemeProvider>
    </NextIntlClientProvider>
  );
};

const customRender = (ui: React.ReactElement, options?: Omit<RenderOptions, 'wrapper'>) =>
  render(ui, { wrapper: AllTheProviders, ...options });

export * from '@testing-library/react';
export { customRender as render }; 