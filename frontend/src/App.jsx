import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import { LMAuthProvider, useLMAuth } from './context/LMAuthContext';

import LMLayout from './components/LMLayout';
import LMDashboard from './pages/LMDashboard';
import LMDoctors from './pages/LMDoctors';
import LMPatients from './pages/LMPatients';
import LMAppointments from './pages/LMAppointments';
import LMMedicalRecords from './pages/LMMedicalRecords';
import LMBilling from './pages/LMBilling';
import LMNotifications from './pages/LMNotifications';
import LMLoginPage from './pages/LMLoginPage';
import LMAddStaff from './pages/LMAddStaff';


// ✅ Protected Route Component
function LMProtectedRoute({ children }) {
  const { user } = useLMAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}


// ✅ Routes Component (THIS WAS MISSING)
function LMAppRoutes() {
  const { user } = useLMAuth();

  return (
    <Routes>

      {/* Login */}
      <Route
        path="/login"
        element={user ? <Navigate to="/" replace /> : <LMLoginPage />}
      />

      {/* Protected Routes */}
      <Route
        path="/"
        element={
          <LMProtectedRoute>
            <LMLayout />
          </LMProtectedRoute>
        }
      >
        <Route index element={<LMDashboard />} />
        <Route path="doctors" element={<LMDoctors />} />
        <Route path="patients" element={<LMPatients />} />
        <Route path="appointments" element={<LMAppointments />} />
        <Route path="medical-records" element={<LMMedicalRecords />} />
        <Route path="billing" element={<LMBilling />} />
        <Route path="notifications" element={<LMNotifications />} />
        <Route path="add-staff" element={<LMAddStaff />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />

    </Routes>
  );
}


// ✅ Main App
export default function App() {
  return (
    <LMAuthProvider>
      <BrowserRouter>
        <LMAppRoutes />
      </BrowserRouter>
    </LMAuthProvider>
  );
}