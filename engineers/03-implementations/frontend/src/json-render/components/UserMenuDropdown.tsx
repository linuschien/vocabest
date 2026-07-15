import React, { useState, useRef, useEffect } from 'react';
import { useStateStore, useStateValue } from '@json-render/react';
import { useNavigate } from 'react-router-dom';
import { User, Settings, Shield, BarChart } from 'lucide-react';

export default function UserMenuDropdown({ element, emit }: any) {
  const [isOpen, setIsOpen] = useState(false);
  const store = useStateStore();
  const dropdownRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const user = useStateValue('/data/user') as any;
  const email = user?.email;
  const isAdmin = useStateValue('/data/isAdmin') as boolean;

  // Handle click outside to close dropdown
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const handleOpenSettings = () => {
    setIsOpen(false);
    if (emit) emit('settings');
  };

  const handleOpenStats = () => {
    setIsOpen(false);
    navigate('/learning-statistics');
  };

  const handleOpenAdmin = () => {
    setIsOpen(false);
    if (emit) emit('admin');
  };

  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <button
        type="button"
        className="inline-flex justify-center items-center rounded-full border border-input bg-background px-6 py-2 text-sm font-medium shadow-sm hover:bg-accent hover:text-accent-foreground focus:outline-none"
        id="avatar-dropdown-btn"
        onClick={() => setIsOpen(!isOpen)}
      >
        <User className="w-4 h-4 mr-2" />
        {email || 'User Menu'}
      </button>

      {isOpen && (
        <div className="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-popover text-popover-foreground shadow-md ring-1 ring-black ring-opacity-5 focus:outline-none border">
          <div className="py-1">
            <button
              onClick={handleOpenStats}
              className="flex items-center w-full px-4 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
            >
              <BarChart className="w-4 h-4 mr-2" />
              學習數據分析
            </button>
            <button
              onClick={handleOpenSettings}
              className="flex items-center w-full px-4 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
            >
              <Settings className="w-4 h-4 mr-2" />
              Personal Settings
            </button>
            {isAdmin && (
              <button
                onClick={handleOpenAdmin}
                className="flex items-center w-full px-4 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
              >
                <Shield className="w-4 h-4 mr-2" />
                Admin Dashboard
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
