"use client";
import React from 'react';
import { WizardContainer } from '@/components/wizard/WizardContainer';
import { Typography, Box, Alert } from '@mui/material';
import { useWizardStore } from '@/lib/store/useWizardStore';

export default function WizardPage() {
  const { currentStep, setStepValid } = useWizardStore();

  // Simulate step validation for demo
  React.useEffect(() => {
    // For demo purposes, mark step 1 as valid after a short delay
    const timer = setTimeout(() => {
      setStepValid(1, true);
    }, 1000);

    return () => clearTimeout(timer);
  }, [setStepValid]);

  const handleNext = () => {
    // This will be implemented in the next step
    console.log('Next step logic will be implemented in Step 3');
  };

  return (
    <WizardContainer
      title={`Step ${currentStep}: Areas Management`}
      subtitle="Configure the main areas for your dashboard"
      onNext={handleNext}
      nextDisabled={false}
    >
      <Box>
        <Alert severity="info" sx={{ mb: 3 }}>
          This is a placeholder for Step {currentStep} content. 
          Areas management interface will be implemented in the next step.
        </Alert>
        
        <Typography variant="body1">
          In this step, you will be able to:
        </Typography>
        <ul>
          <li>Create up to 5 main areas for your dashboard</li>
          <li>Define names and descriptions for each area</li>
          <li>Set up the foundation for your indicator groupings</li>
        </ul>
      </Box>
    </WizardContainer>
  );
} 