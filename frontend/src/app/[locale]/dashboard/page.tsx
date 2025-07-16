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
  CircularProgress,
  Alert
} from '@mui/material';
import { 
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon
} from '@mui/icons-material';
import { CircularLayout, GoalsSidebar } from '@/components/dashboard';
import { useDashboardWithRelationships } from '@/hooks';
import { useDashboardEditMode } from '@/hooks/useDashboardEditMode';
import { useRouter } from 'next/navigation';

const DRAWER_WIDTH = 320;

export default function DashboardPage() {
  const router = useRouter();
  const { isEditMode } = useDashboardEditMode();
  const [highlightedGoals, setHighlightedGoals] = useState<string[]>([]);
  const [highlightedSubareas, setHighlightedSubareas] = useState<string[]>([]);
  const [sidebarOpen, setSidebarOpen] = useState(true);

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
      {goals && goals.length > 0 && (
        <Drawer
          variant="persistent"
          open={sidebarOpen}
          sx={{
            width: sidebarOpen ? DRAWER_WIDTH : 0,
            flexShrink: 0,
            '& .MuiDrawer-paper': {
              width: DRAWER_WIDTH,
              boxSizing: 'border-box',
              borderRight: '1px solid rgba(0, 0, 0, 0.12)',
              transition: 'width 0.2s',
              overflow: 'visible',
            },
          }}
        >
          <Toolbar sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
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
          {/* Close button, vertically centered on the outer right edge of the sidebar */}
          {sidebarOpen && (
            <Box
              sx={{
                position: 'fixed',
                top: '50%',
                left: DRAWER_WIDTH - 24, // 24 = half the button size for overlap
                transform: 'translateY(-50%)',
                zIndex: 1300,
              }}
            >
              <IconButton onClick={() => setSidebarOpen(false)} size="large" title="Close sidebar" sx={{ bgcolor: '#fff', border: '1px solid #eee', boxShadow: 2 }}>
                <ChevronLeftIcon />
              </IconButton>
            </Box>
          )}
        </Drawer>
      )}
      {/* Sidebar open button (when closed) */}
      {!sidebarOpen && (
        <Box
          sx={{
            position: 'fixed',
            top: '50%',
            left: 0,
            transform: 'translateY(-50%)',
            zIndex: 1300,
          }}
        >
          <IconButton onClick={() => setSidebarOpen(true)} size="large" color="primary" title="Open sidebar" sx={{ bgcolor: '#fff', border: '1px solid #eee', boxShadow: 2 }}>
            <ChevronRightIcon />
          </IconButton>
        </Box>
      )}

      {/* Main Dashboard Content */}
      <Box component="main" sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
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
              goals={goals}
              relationships={relationships}
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