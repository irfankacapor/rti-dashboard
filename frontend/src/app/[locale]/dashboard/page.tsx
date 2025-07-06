"use client";
import React, { useState } from 'react';
import { 
  Box, 
  Container, 
  Typography, 
  Paper,
  Drawer,
  IconButton,
  Toolbar,
  AppBar,
  CircularProgress,
  Alert
} from '@mui/material';
import { 
  Edit as EditIcon
} from '@mui/icons-material';
import { CircularLayout, GoalsSidebar } from '@/components/dashboard';
import { useDashboardWithRelationships } from '@/hooks';
import { useRouter } from 'next/navigation';

const DRAWER_WIDTH = 320;

export default function DashboardPage() {
  const router = useRouter();
  const [isEditMode, setIsEditMode] = useState(false);
  const [highlightedGoals, setHighlightedGoals] = useState<string[]>([]);
  const [highlightedSubareas, setHighlightedSubareas] = useState<string[]>([]);

  // Fetch dashboard data with relationships
  const { 
    areas, 
    subareas, 
    goals, 
    goalGroups, 
    relationships,
    isLoading: isLoadingDashboard, 
    error: dashboardError 
  } = useDashboardWithRelationships();

  const handleSubareaClick = (subareaId: string) => {
    router.push(`/en/dashboard/subarea/${subareaId}`);
  };

  const handleGoalHover = (goalIds: string[]) => {
    setHighlightedGoals(goalIds);
    // Find subareas linked to these goals using relationship mappings
    const linkedSubareaIds = goalIds.flatMap(goalId => 
      relationships.goalToSubareas[goalId] || []
    );
    setHighlightedSubareas(linkedSubareaIds);
  };

  const handleGoalLeave = () => {
    setHighlightedGoals([]);
    setHighlightedSubareas([]);
  };

  const handleSubareaHover = (subareaId: string) => {
    setHighlightedSubareas([subareaId]);
    // Find goals linked to this subarea using relationship mappings
    const linkedGoalIds = relationships.subareaToGoals[subareaId] || [];
    setHighlightedGoals(linkedGoalIds);
  };

  const handleSubareaLeave = () => {
    setHighlightedGoals([]);
    setHighlightedSubareas([]);
  };

  const handleEditModeToggle = () => {
    setIsEditMode(!isEditMode);
  };

  if (isLoadingDashboard) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (dashboardError) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">
          {dashboardError || 'Failed to load dashboard data'}
        </Alert>
      </Container>
    );
  }

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      {/* Goals Sidebar */}
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            borderRight: '1px solid rgba(0, 0, 0, 0.12)',
          },
        }}
      >
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Goals
          </Typography>
        </Toolbar>
        <GoalsSidebar
          goals={goals || []}
          goalGroups={goalGroups || []}
          highlightedGoals={highlightedGoals}
          onGoalHover={handleGoalHover}
          onGoalLeave={handleGoalLeave}
        />
      </Drawer>

      {/* Main Dashboard Content */}
      <Box component="main" sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Top App Bar */}
        <AppBar position="static" elevation={1} color="default">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              RTI Dashboard
            </Typography>
            <IconButton
              color={isEditMode ? "primary" : "default"}
              onClick={handleEditModeToggle}
              title={isEditMode ? "Exit Edit Mode" : "Enter Edit Mode"}
            >
              <EditIcon />
            </IconButton>
          </Toolbar>
        </AppBar>

        {/* Dashboard Content */}
        <Box sx={{ flexGrow: 1, p: 3, overflow: 'auto' }}>
          {areas && areas.length > 0 ? (
            <CircularLayout
              areas={areas}
              subareas={subareas || []}
              isEditMode={isEditMode}
              highlightedSubareas={highlightedSubareas}
              onSubareaClick={handleSubareaClick}
              onSubareaHover={handleSubareaHover}
              onSubareaLeave={handleSubareaLeave}
            />
          ) : (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography variant="h5" gutterBottom>
                No Dashboard Data
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Complete the setup wizard to configure your dashboard.
              </Typography>
            </Paper>
          )}
        </Box>
      </Box>
    </Box>
  );
} 