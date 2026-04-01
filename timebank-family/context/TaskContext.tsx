import React, { createContext, useContext, useState, ReactNode } from 'react';
import { Task, TaskType, TaskStatus } from '../types';

// Initial tasks (empty, will be loaded from API)
const INITIAL_TASKS: Task[] = [];

interface TaskContextType {
  tasks: Task[];
  addTask: (task: Task) => void;
  acceptTask: (taskId: string, userId: string, userName: string, userAvatar: string) => boolean;
  submitEvidence: (taskId: string, photos: string[], checkInTime?: number) => void;
  confirmTask: (taskId: string, rating: number, review: string) => void;
  appealTask: (taskId: string, reason: string) => void;
  getTaskById: (id: string) => Task | undefined;
}

const TaskContext = createContext<TaskContextType | undefined>(undefined);

export const TaskProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [tasks, setTasks] = useState<Task[]>(INITIAL_TASKS);

  const addTask = (task: Task) => {
    setTasks(prev => [task, ...prev]);
  };

  const acceptTask = (taskId: string, userId: string, userName: string, userAvatar: string) => {
    let success = false;
    setTasks(prev => prev.map(task => {
      if (task.id === taskId && task.status === 'pending') {
        if (task.publisherId === userId) return task;
        success = true;
        return {
          ...task,
          status: 'accepted',
          acceptorId: userId,
          acceptorName: userName,
          acceptorAvatar: userAvatar
        };
      }
      return task;
    }));
    return success;
  };

  const submitEvidence = (taskId: string, photos: string[], checkInTime?: number) => {
    setTasks(prev => prev.map(task => {
      if (task.id === taskId) {
        return {
          ...task,
          status: 'waiting_acceptance',
          evidencePhotos: photos,
          checkInTime: checkInTime || task.checkInTime,
          finishTime: Date.now()
        };
      }
      return task;
    }));
  };

  const confirmTask = (taskId: string, rating: number, review: string) => {
    setTasks(prev => prev.map(task => {
      if (task.id === taskId) {
        return {
          ...task,
          status: 'completed',
          rating,
          review
        };
      }
      return task;
    }));
  };

  const appealTask = (taskId: string, reason: string) => {
    setTasks(prev => prev.map(task => {
      if (task.id === taskId) {
        return {
          ...task,
          status: 'appealing',
          appealReason: reason
        };
      }
      return task;
    }));
  };

  const getTaskById = (id: string) => tasks.find(t => t.id === id);

  return (
    <TaskContext.Provider value={{
      tasks,
      addTask,
      acceptTask,
      submitEvidence,
      confirmTask,
      appealTask,
      getTaskById
    }}>
      {children}
    </TaskContext.Provider>
  );
};

export const useTasks = () => {
  const context = useContext(TaskContext);
  if (!context) {
    throw new Error('useTasks must be used within a TaskProvider');
  }
  return context;
};
