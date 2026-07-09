import React from 'react';
import { useStateStore } from '@json-render/react';

export default function AlertDialog({ element, children, emit }: any) {
  const store = useStateStore();
  const { id, label } = element?.props ?? {};
  
  // Check if open
  const isOpen = store.get(`/modals/${id}`);
  
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" id={id}>
      <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
        <h2 className="text-lg font-bold mb-4 text-red-600">{label || 'Confirm Action'}</h2>
        <div className="mb-6 text-gray-600">
          {children}
        </div>
        <button 
          className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
          onClick={() => store.set(`/modals/${id}`, false)}
        >
          ✕
        </button>
      </div>
    </div>
  );
}
