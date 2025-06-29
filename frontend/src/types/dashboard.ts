export interface DashboardArea {
  id: string;
  name: string;
  description: string;
  code: string;
  position: {
    x: number;
    y: number;
  };
  performance: {
    score: number;
    colorCode: string;
  };
  subareaCount: number;
}

export interface DashboardSubarea {
  id: string;
  name: string;
  description: string;
  code: string;
  areaId: string;
  areaName: string;
  position: {
    x: number;
    y: number;
  };
  performance: {
    score: number;
    colorCode: string;
  };
  indicatorCount: number;
  goalCount: number;
}

export interface DashboardGoal {
  id: string;
  name: string;
  description: string;
  goalGroupId: string;
  goalGroupName: string;
  targetYear: number;
  targetValue: number;
  currentValue: number;
  linkedSubareaIds: string[];
}

export interface GoalGroup {
  id: string;
  name: string;
  description: string;
  goals: DashboardGoal[];
}

export interface PerformanceMetric {
  subareaId: string;
  subareaName: string;
  currentScore: number;
  colorCode: string;
  trend: 'up' | 'down' | 'stable';
  lastUpdated: string;
}

export interface PerformanceMetricsResponse {
  areaId: string;
  metrics: PerformanceMetric[];
  averageScore: number;
}

export interface CircularLayoutProps {
  areas: DashboardArea[];
  subareas: DashboardSubarea[];
  isEditMode: boolean;
  highlightedSubareas: string[];
  onSubareaClick: (subareaId: string) => void;
  onSubareaHover: (subareaId: string) => void;
}

export interface GoalsSidebarProps {
  goals: DashboardGoal[];
  goalGroups: GoalGroup[];
  highlightedGoals: string[];
  onGoalHover: (goalIds: string[]) => void;
} 