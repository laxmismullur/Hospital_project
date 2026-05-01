import React, { useState } from 'react';
import { LMApi } from '../services/LMApiService';
import { Plus, X } from 'lucide-react';

import LMDoctors from './LMDoctors';
import LMNurses from './LMNurses';
import LMReceptionists from './LMReceptionists';

/* ── Specializations ── */
const SPECIALIZATIONS = [
  'Cardiology','Neurology','Pediatrics','Orthopedics','Dermatology',
  'General Medicine','ENT','Gynecology','Ophthalmology','Psychiatry',
  'Oncology','Urology','Endocrinology','Pulmonology','Nephrology',
];

/* ── Department Map ── */
const DEPT_MAP = {
  Cardiology: 'Cardiology',
  Neurology: 'Neurology',
  Pediatrics: 'Pediatrics',
  Orthopedics: 'Orthopedics',
  Dermatology: 'Dermatology',
  'General Medicine': 'General Medicine',
  ENT: 'ENT',
  Gynecology: 'Gynecology',
  Ophthalmology: 'Ophthalmology',
};

export default function AddStaff() {

  const [tab, setTab] = useState('DOCTOR');
  const [showModal, setShowModal] = useState(false);
  const [role, setRole] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);

  const [form, setForm] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    phone: '',
    qualification: '',
    department: '',
    specialization: '',
    consultationFee: '',
    experience: '',
    availability: '',
    address: '',
    active: true   // ✅ ADDED
  });

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const resetForm = () => {
    setForm({
      fullName: '',
      username: '',
      email: '',
      password: '',
      phone: '',
      qualification: '',
      department: '',
      specialization: '',
      consultationFee: '',
      experience: '',
      availability: '',
      address: '',
      active: true   // ✅ ADDED
    });
    setRole('');
  };

  /* ── SAVE ── */
  const handleSave = async () => {

    if (!role) return alert("Select role");

    if (!form.fullName || !form.username || !form.password || !form.phone) {
      return alert("Please fill required fields");
    }

    if (role === "DOCTOR" && !form.specialization) {
      return alert("Select specialization");
    }

    try {

      if (role === "DOCTOR") {

        await LMApi.createDoctor({
          fullName: form.fullName,
          specialization: form.specialization,
          department: form.department,
          email: form.email,
          phone: form.phone,
          qualification: form.qualification,
          experience: form.experience,
          consultationFee: form.consultationFee,
          availability: form.availability,
          address: form.address,
          active: form.active   // ✅ ADDED
        });

      } else {

        await LMApi.addStaff({
          fullName: form.fullName,
          username: form.username,
          email: form.email,
          password: form.password,
          role: role,
          phone: form.phone,
          department: form.department || "General",
          active: form.active   // ✅ ADDED
        });
      }

      alert("Saved successfully ✅");

      resetForm();
      setShowModal(false);
      setRefreshKey(prev => prev + 1);

    } catch (e) {
      console.error("SAVE ERROR:", e.response?.data || e.message);
      alert("Error saving ❌");
    }
  };

  return (
    <div>

      {/* HEADER */}
      <div className="page-header">
        <h1>Staff Management</h1>

        <button
          className="btn btn-primary"
          onClick={() => {
            setRole(tab);
            setShowModal(true);
          }}
        >
          <Plus size={14} /> Add Staff
        </button>
      </div>

      {/* TABS */}
      <div className="tabs">
        <button className={`tab-btn ${tab === 'DOCTOR' ? 'active' : ''}`} onClick={() => setTab('DOCTOR')}>
          Doctors
        </button>

        <button className={`tab-btn ${tab === 'NURSE' ? 'active' : ''}`} onClick={() => setTab('NURSE')}>
          Nurses
        </button>

        <button className={`tab-btn ${tab === 'RECEPTIONIST' ? 'active' : ''}`} onClick={() => setTab('RECEPTIONIST')}>
          Receptionists
        </button>
      </div>

      {/* CONTENT */}
      <div style={{ marginTop: '20px' }}>
        {tab === 'DOCTOR' && <LMDoctors key={refreshKey} />}
        {tab === 'NURSE' && <LMNurses key={refreshKey} />}
        {tab === 'RECEPTIONIST' && <LMReceptionists key={refreshKey} />}
      </div>

      {/* MODAL */}
      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal" style={{ maxWidth: '600px' }}>

            <div className="modal-header">
              <h3>Add New {role || 'Staff'}</h3>
              <button className="close-btn" onClick={() => setShowModal(false)}>
                <X size={16} />
              </button>
            </div>

            <div className="modal-body">

              {/* ROLE */}
              <div className="form-group">
                <label>Role *</label>
                <select value={role} onChange={e => setRole(e.target.value)}>
                  <option value="">Select Role</option>
                  <option value="DOCTOR">Doctor</option>
                  <option value="NURSE">Nurse</option>
                  <option value="RECEPTIONIST">Receptionist</option>
                </select>
              </div>

              {/* ACTIVE / INACTIVE (ADDED) */}
              <div className="form-group">
                <label>Status</label>
                <select value={form.active} onChange={e => set('active', e.target.value === 'true')}>
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </div>

              {/* BASIC */}
              <div className="form-row">
                <div className="form-group">
                  <label>Full Name *</label>
                  <input value={form.fullName} onChange={e => set('fullName', e.target.value)} />
                </div>

                <div className="form-group">
                  <label>Username *</label>
                  <input value={form.username} onChange={e => set('username', e.target.value)} />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Email</label>
                  <input value={form.email} onChange={e => set('email', e.target.value)} />
                </div>

                <div className="form-group">
                  <label>Phone *</label>
                  <input value={form.phone} onChange={e => set('phone', e.target.value)} />
                </div>
              </div>

              <div className="form-group">
                <label>Password *</label>
                <input type="password" value={form.password} onChange={e => set('password', e.target.value)} />
              </div>

              {/* DOCTOR EXTRA */}
              {role === "DOCTOR" && (
                <>
                  <div className="form-group">
                    <label>Qualification</label>
                    <input value={form.qualification} onChange={e => set('qualification', e.target.value)} />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Experience</label>
                      <input value={form.experience} onChange={e => set('experience', e.target.value)} />
                    </div>

                    <div className="form-group">
                      <label>Fee (₹)</label>
                      <input value={form.consultationFee} onChange={e => set('consultationFee', e.target.value)} />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Availability</label>
                    <input value={form.availability} onChange={e => set('availability', e.target.value)} />
                  </div>

                  <div className="form-group">
                    <label>Address</label>
                    <input value={form.address} onChange={e => set('address', e.target.value)} />
                  </div>
                </>
              )}

              {/* SPECIALIZATION */}
              {(role === "DOCTOR" || role === "NURSE") && (
                <div className="form-row">
                  <div className="form-group">
                    <label>Department</label>
                    <input value={form.department} readOnly />
                  </div>

                  <div className="form-group">
                    <label>Specialization</label>
                    <select
                      value={form.specialization}
                      onChange={e => {
                        const val = e.target.value;
                        set('specialization', val);
                        set('department', DEPT_MAP[val] || val);
                      }}
                    >
                      <option value="">Select Specialization</option>
                      {SPECIALIZATIONS.map(s => <option key={s}>{s}</option>)}
                    </select>
                  </div>
                </div>
              )}

            </div>

            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setShowModal(false)}>
                Cancel
              </button>

              <button className="btn btn-primary" onClick={handleSave}>
                Save
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}