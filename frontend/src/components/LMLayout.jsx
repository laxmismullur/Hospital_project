import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useLMAuth } from '../context/LMAuthContext';
import {
  LayoutDashboard, Users, CalendarDays, FileText,
  Stethoscope, LogOut, Menu, X, Bell, ChevronDown, Activity, DollarSign
} from 'lucide-react';

const NAV = [
  { path: '/',                icon: LayoutDashboard, label: 'Dashboard',       roles: ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT'] },
  { path: '/doctors',         icon: Stethoscope,     label: 'Doctors',         roles: ['ADMIN'] },
  { path: '/patients',        icon: Users,           label: 'Patients',        roles: ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT'] },

  // ✅ FIXED: PATIENT ADDED HERE
  { path: '/appointments',    icon: CalendarDays,    label: 'Appointments',    roles: ['ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT'] },

  { path: '/medical-records', icon: FileText,        label: 'Medical Records', roles: ['ADMIN','DOCTOR','NURSE'] },
  { path: '/billing',         icon: DollarSign,      label: 'Billing',         roles: ['ADMIN','RECEPTIONIST'] },
  { path: '/notifications',   icon: Bell,            label: 'Notifications',   roles: ['PATIENT'] },
  { path: '/add-staff', label: 'Add Staff', icon: Users, roles: ['ADMIN'] },
];

const ROLE_COLOR = {
  ADMIN: '#f59e0b',
  DOCTOR: '#14b8a6',
  NURSE: '#a855f7',
  RECEPTIONIST: '#3b82f6',
  PATIENT: '#22c55e',
};

export default function LMLayout() {
  const { user, logout } = useLMAuth();
  const navigate = useNavigate();

  const [collapsed, setCollapsed] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const roleColor = ROLE_COLOR[user?.role] || '#f59e0b';

  const initials =
    user?.fullName
      ?.split(' ')
      .map(w => w[0])
      .join('')
      .slice(0, 2)
      .toUpperCase() || 'LM';

  // ✅ FILTER BASED ON ROLE
  const allowed = NAV.filter(n => n.roles.includes(user?.role));

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', height: '100vh' }}>

      {/* SIDEBAR */}
      <aside style={{
        width: collapsed ? '64px' : '220px',
        background: '#0a0f18',
        borderRight: '1px solid #1e2a3a',
        display: 'flex',
        flexDirection: 'column'
      }}>

        {/* LOGO */}
        <div style={{
          height: '64px',
          display: 'flex',
          alignItems: 'center',
          padding: '0 12px',
          borderBottom: '1px solid #1e2a3a'
        }}>
          <strong style={{ color: '#fff' }}>LM</strong>
          {!collapsed && <span style={{ marginLeft: '10px', color: '#fff' }}>Hospital</span>}
        </div>

        {/* MENU */}
        <nav style={{ flex: 1, padding: '10px' }}>
          {allowed.map(({ path, icon: Icon, label }) => (
            <NavLink
              key={path}
              to={path}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                padding: '8px',
                marginBottom: '5px',
                textDecoration: 'none',
                color: isActive ? '#fff' : '#5a7394',
                background: isActive ? '#1a2535' : 'transparent',
                borderRadius: '6px'
              })}
            >
              <Icon size={16} />
              {!collapsed && label}
            </NavLink>
          ))}
        </nav>

        {/* LOGOUT */}
        <button onClick={handleLogout} style={{
          margin: '10px',
          padding: '8px',
          background: 'none',
          border: '1px solid #ef4444',
          color: '#ef4444',
          cursor: 'pointer',
          borderRadius: '6px'
        }}>
          Logout
        </button>

      </aside>

      {/* MAIN */}
      <div style={{ flex: 1 }}>

        {/* HEADER */}
        <header style={{
          height: '60px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 20px',
          borderBottom: '1px solid #1e2a3a'
        }}>
          <button onClick={() => setCollapsed(!collapsed)}>
            {collapsed ? <Menu size={18}/> : <X size={18}/>}
          </button>

          <div>
            {user?.fullName} ({user?.role})
          </div>
        </header>

        {/* PAGE CONTENT */}
        <main style={{ padding: '20px' }}>
          <Outlet />
        </main>

      </div>
    </div>
  );
}