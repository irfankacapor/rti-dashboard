// Temporary Goal type definition (replace with import from @/types/goals if/when available)
export interface Goal {
  id: number;
  name: string;
  description?: string;
  type?: string;
  goalGroup?: GoalGroup;
  url?: string;
  year?: number;
  createdAt?: string;
  targetCount?: number;
}

export interface GoalGroup {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  goalCount?: number;
}

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export const goalService = {
  getGoals: async (): Promise<Goal[]> => {
    const response = await fetch(`${API_BASE}/goals`);
    if (!response.ok) {
      throw new Error('Failed to fetch goals');
    }
    return response.json();
  },
  createGoal: async (goal: any) => {
    const response = await fetch(`${API_BASE}/goals`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(goal),
    });
    if (!response.ok) {
      throw new Error('Failed to create goal');
    }
    return response.json();
  },
  updateGoal: async (id: number, goal: any) => {
    const response = await fetch(`${API_BASE}/goals/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(goal),
    });
    if (!response.ok) {
      throw new Error('Failed to update goal');
    }
    return response.json();
  },
  deleteGoal: async (id: number) => {
    const response = await fetch(`${API_BASE}/goals/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete goal');
    }
    return true;
  },
  getGroups: async (): Promise<GoalGroup[]> => {
    const response = await fetch(`${API_BASE}/goal-groups`);
    if (!response.ok) {
      throw new Error('Failed to fetch goal groups');
    }
    return response.json();
  },
  createGroup: async (name: string, description?: string): Promise<GoalGroup> => {
    const response = await fetch(`${API_BASE}/goal-groups`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description }),
    });
    if (!response.ok) {
      throw new Error('Failed to create goal group');
    }
    return response.json();
  },
  getGoalTargets: async (goalId: number): Promise<any[]> => {
    const response = await fetch(`${API_BASE}/goals/${goalId}/targets`);
    if (!response.ok) {
      throw new Error('Failed to fetch goal targets');
    }
    return response.json();
  },
}; 